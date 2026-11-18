//! Bounded host-to-watch metadata and artwork protocol.
//! Artwork: 32x32 RGB565, little-endian (2048 bytes total).

pub const TITLE_MAX: usize = 48;
pub const ART_WIDTH: usize = 32;
pub const ART_HEIGHT: usize = 32;
pub const ART_BYTES: usize = ART_WIDTH * ART_HEIGHT * 2;
pub const MSG_METADATA: u8 = 0x01;
pub const MSG_ART_BEGIN: u8 = 0x02;
pub const MSG_ART_CHUNK: u8 = 0x03;
pub const MSG_ART_CLEAR: u8 = 0x04;

#[derive(Clone, Copy, Debug, PartialEq)]
pub enum PacketError { Empty, Malformed, Unsupported, ArtSequence }

pub struct Model {
    pub title: [u8; TITLE_MAX], pub title_len: usize,
    pub artist: [u8; TITLE_MAX], pub artist_len: usize,
    pub playing: bool, pub phone_available: bool,
    pub art: [u8; ART_BYTES], pub art_received: usize, pub art_ready: bool,
}
impl Model {
    pub const fn new() -> Self {
        Self { title:[0;TITLE_MAX], title_len:0, artist:[0;TITLE_MAX], artist_len:0,
            playing:false, phone_available:false, art:[0;ART_BYTES], art_received:0, art_ready:false }
    }
    pub fn title_str(&self) -> &str { core::str::from_utf8(&self.title[..self.title_len]).unwrap_or("Unknown track") }
    pub fn artist_str(&self) -> &str { core::str::from_utf8(&self.artist[..self.artist_len]).unwrap_or("Unknown artist") }
    pub fn apply(&mut self, p: &[u8]) -> Result<(),PacketError> {
        let Some(kind)=p.first().copied() else { return Err(PacketError::Empty) };
        match kind {
            MSG_METADATA => {
                if p.len()<4 { return Err(PacketError::Malformed) }
                let (state,tl,al)=(p[1],p[2] as usize,p[3] as usize);
                if state>2 || tl>TITLE_MAX || al>TITLE_MAX || p.len()!=4+tl+al { return Err(PacketError::Malformed) }
                let split=4+tl;
                if core::str::from_utf8(&p[4..split]).is_err() || core::str::from_utf8(&p[split..]).is_err() { return Err(PacketError::Malformed) }
                self.title[..tl].copy_from_slice(&p[4..split]); self.artist[..al].copy_from_slice(&p[split..]);
                self.title_len=tl; self.artist_len=al; self.phone_available=state!=0; self.playing=state==2; Ok(())
            }
            MSG_ART_BEGIN => {
                if p.len()!=3 || p[1] as usize!=ART_WIDTH || p[2] as usize!=ART_HEIGHT { return Err(PacketError::Malformed) }
                self.art.fill(0); self.art_received=0; self.art_ready=false; Ok(())
            }
            MSG_ART_CHUNK => {
                if p.len()<=3 { return Err(PacketError::Malformed) }
                let off=u16::from_le_bytes([p[1],p[2]]) as usize; let data=&p[3..];
                if off!=self.art_received || off+data.len()>ART_BYTES { return Err(PacketError::ArtSequence) }
                self.art[off..off+data.len()].copy_from_slice(data); self.art_received+=data.len();
                self.art_ready=self.art_received==ART_BYTES; Ok(())
            }
            MSG_ART_CLEAR => {
                if p.len()!=1 { return Err(PacketError::Malformed) }
                self.art.fill(0); self.art_received=0; self.art_ready=false; Ok(())
            }
            _ => Err(PacketError::Unsupported),
        }
    }
}
#[cfg(test)]
mod tests {
    use super::*;
    #[test] fn metadata_and_state() {
        let mut m=Model::new(); let mut p=[0u8;16]; p[..4].copy_from_slice(&[MSG_METADATA,2,6,6]);
        p[4..10].copy_from_slice(b"Track1"); p[10..16].copy_from_slice(b"Artist");
        assert_eq!(m.apply(&p),Ok(())); assert_eq!(m.title_str(),"Track1"); assert_eq!(m.artist_str(),"Artist");
        assert!(m.phone_available && m.playing);
    }
    #[test] fn malformed_metadata_rejected() { let mut m=Model::new(); assert_eq!(m.apply(&[MSG_METADATA,2,48,48]),Err(PacketError::Malformed)); }
    #[test] fn artwork_is_sequential_and_bounded() {
        let mut m=Model::new(); assert_eq!(m.apply(&[MSG_ART_BEGIN,32,32]),Ok(()));
        assert_eq!(m.apply(&[MSG_ART_CHUNK,1,0,7]),Err(PacketError::ArtSequence));
        let mut off=0usize;
        while off<ART_BYTES { let n=(ART_BYTES-off).min(125); let mut p=[0u8;128]; p[0]=MSG_ART_CHUNK;
            p[1..3].copy_from_slice(&(off as u16).to_le_bytes()); p[3..3+n].fill(0x5a);
            assert_eq!(m.apply(&p[..3+n]),Ok(())); off+=n; }
        assert!(m.art_ready); assert_eq!(m.art_received,ART_BYTES);
    }
    #[test] fn clear_art() { let mut m=Model::new(); m.art_ready=true; assert_eq!(m.apply(&[MSG_ART_CLEAR]),Ok(())); assert!(!m.art_ready); }
}
