//! Pure, bounded protocol logic for the Nothing Music watch app.
//! Host messages are untrusted; every length, UTF-8 sequence and art offset is checked.

pub const TEXT_MAX: usize = 48;
pub const ART_W: usize = 32;
pub const ART_H: usize = 32;
pub const ART_BYTES: usize = ART_W * ART_H * 2;
pub const MSG_MAX: usize = 128;

const MAGIC: &[u8; 3] = b"NM1";
const META: u8 = b'M';
const ART_BEGIN: u8 = b'B';
const ART_CHUNK: u8 = b'C';
const ART_CLEAR: u8 = b'X';

#[derive(Clone, Copy, Debug, PartialEq, Eq)]
pub enum Error {
    Empty,
    Unknown,
    Malformed,
    InvalidUtf8,
    ArtOrder,
}

pub struct Model {
    pub title: [u8; TEXT_MAX],
    pub title_len: usize,
    pub artist: [u8; TEXT_MAX],
    pub artist_len: usize,
    pub phone_available: bool,
    pub playing: bool,
    pub art: [u8; ART_BYTES],
    pub art_len: usize,
    pub art_ready: bool,
}

impl Model {
    pub const fn new() -> Self {
        Self {
            title: [0; TEXT_MAX],
            title_len: 0,
            artist: [0; TEXT_MAX],
            artist_len: 0,
            phone_available: false,
            playing: false,
            art: [0; ART_BYTES],
            art_len: 0,
            art_ready: false,
        }
    }

    pub fn title(&self) -> &str {
        core::str::from_utf8(&self.title[..self.title_len]).unwrap_or("Unknown track")
    }

    pub fn artist(&self) -> &str {
        core::str::from_utf8(&self.artist[..self.artist_len]).unwrap_or("Unknown artist")
    }

    pub fn set_media_state(&mut self, playing: bool, available: bool) {
        self.playing = playing;
        self.phone_available = available;
    }

    pub fn apply(&mut self, packet: &[u8]) -> Result<(), Error> {
        if packet.len() < 4 {
            return Err(if packet.is_empty() { Error::Empty } else { Error::Malformed });
        }
        if &packet[..3] != MAGIC {
            return Err(Error::Unknown);
        }

        match packet[3] {
            META => {
                if packet.len() < 7 {
                    return Err(Error::Malformed);
                }
                let state = packet[4];
                let title_len = packet[5] as usize;
                let artist_len = packet[6] as usize;
                if state > 2 || title_len > TEXT_MAX || artist_len > TEXT_MAX
                    || packet.len() != 7 + title_len + artist_len {
                    return Err(Error::Malformed);
                }
                let split = 7 + title_len;
                let title = &packet[7..split];
                let artist = &packet[split..];
                if core::str::from_utf8(title).is_err() || core::str::from_utf8(artist).is_err() {
                    return Err(Error::InvalidUtf8);
                }
                self.title[..title_len].copy_from_slice(title);
                self.artist[..artist_len].copy_from_slice(artist);
                self.title_len = title_len;
                self.artist_len = artist_len;
                self.phone_available = state != 0;
                self.playing = state == 2;
                Ok(())
            }
            ART_BEGIN => {
                if packet.len() != 6 || packet[4] as usize != ART_W || packet[5] as usize != ART_H {
                    return Err(Error::Malformed);
                }
                self.art.fill(0);
                self.art_len = 0;
                self.art_ready = false;
                Ok(())
            }
            ART_CHUNK => {
                if packet.len() <= 6 {
                    return Err(Error::Malformed);
                }
                let offset = u16::from_le_bytes([packet[4], packet[5]]) as usize;
                let bytes = &packet[6..];
                if offset != self.art_len || offset + bytes.len() > ART_BYTES {
                    return Err(Error::ArtOrder);
                }
                self.art[offset..offset + bytes.len()].copy_from_slice(bytes);
                self.art_len += bytes.len();
                self.art_ready = self.art_len == ART_BYTES;
                Ok(())
            }
            ART_CLEAR => {
                if packet.len() != 4 {
                    return Err(Error::Malformed);
                }
                self.art.fill(0);
                self.art_len = 0;
                self.art_ready = false;
                Ok(())
            }
            _ => Err(Error::Unknown),
        }
    }
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn parses_track_metadata_and_state() {
        let mut m = Model::new();
        let p = b"NM1M\x02\x06\x06Track1Artist";
        assert_eq!(m.apply(p), Ok(()));
        assert_eq!(m.title(), "Track1");
        assert_eq!(m.artist(), "Artist");
        assert!(m.phone_available);
        assert!(m.playing);
    }

    #[test]
    fn rejects_invalid_metadata_lengths_and_utf8() {
        let mut m = Model::new();
        assert_eq!(m.apply(b"NM1M\x02\x30\x30"), Err(Error::Malformed));
        assert_eq!(m.apply(b"NM1M\x02\x01\x00\xff"), Err(Error::InvalidUtf8));
    }

    #[test]
    fn art_must_arrive_in_order_and_with_exact_size() {
        let mut m = Model::new();
        assert_eq!(m.apply(b"NM1B\x20\x20"), Ok(()));
        assert_eq!(m.apply(b"NM1C\x01\x00\x01"), Err(Error::ArtOrder));
        let mut offset = 0usize;
        while offset < ART_BYTES {
            let n = (ART_BYTES - offset).min(MSG_MAX - 6);
            let mut p = [0u8; MSG_MAX];
            p[..4].copy_from_slice(b"NM1C");
            p[4..6].copy_from_slice(&(offset as u16).to_le_bytes());
            p[6..6 + n].fill(0x5a);
            assert_eq!(m.apply(&p[..6 + n]), Ok(()));
            offset += n;
        }
        assert!(m.art_ready);
        assert_eq!(m.art_len, ART_BYTES);
    }

    #[test]
    fn clear_art_resets_transfer() {
        let mut m = Model::new();
        m.art_ready = true;
        m.art_len = ART_BYTES;
        assert_eq!(m.apply(b"NM1X"), Ok(()));
        assert!(!m.art_ready);
        assert_eq!(m.art_len, 0);
    }
}
