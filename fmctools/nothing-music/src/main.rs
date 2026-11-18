#![cfg_attr(any(target_os = "none", wp2_sim), no_main, cfg_attr(target_os = "none", no_std))]
mod protocol;
#[cfg(not(any(target_os = "none", wp2_sim)))]
fn main() {}

#[cfg(any(target_os = "none", wp2_sim))]
mod device {
    use crate::protocol::{Model, ART_HEIGHT, ART_WIDTH};
    use cmf_wp2_app::{Api,BUTTON,CROWN,MEDIA,MEDIA_NEXT,MEDIA_PAUSE,MEDIA_PLAY,MEDIA_PREV,MESSAGE,PAUSE,RESUME,TOUCH_DOWN};
    cmf_wp2_app::entry!(main);
    cmf_wp2_app::app_icon!();

    const BLACK:u16=0; const WHITE:u16=0xffff; const GREY:u16=0x8410; const PANEL:u16=0x2124; const ACCENT:u16=0xfd20;
    const CELL:i16=4; const ART_X:i16=169; const ART_Y:i16=88;

    fn center(api:&Api,y:i16,s:&str,c:u16) {
        let mut n=s.len().min(28);
        while !s.is_char_boundary(n) { n-=1; }
        let shown=&s[..n];
        api.text(api.width as i16/2-8*shown.len() as i16,y,shown,c);
    }
    fn art(api:&Api,m:&Model) {
        if !m.art_ready {
            api.rect(ART_X,ART_Y,128,128,PANEL);
            api.rect(ART_X+48,ART_Y+35,32,5,ACCENT); api.rect(ART_X+58,ART_Y+40,5,38,ACCENT);
            api.rect(ART_X+43,ART_Y+78,42,5,ACCENT); center(api,ART_Y+145,"COVER NOT RECEIVED",GREY); return;
        }
        for y in 0..ART_HEIGHT { for x in 0..ART_WIDTH {
            let i=(y*ART_WIDTH+x)*2; let color=u16::from_le_bytes([m.art[i],m.art[i+1]]);
            api.rect(ART_X+x as i16*CELL,ART_Y+y as i16*CELL,CELL as u16,CELL as u16,color);
        }}
    }
    fn draw(api:&Api,m:&Model,status:&str) {
        api.rect(0,0,api.width,api.height,BLACK); center(api,28,"NOTHING MUSIC",WHITE); center(api,54,"PHONE PLAYER",GREY);
        art(api,m);
        center(api,253,if m.title_len==0 {"Waiting for track"} else {m.title_str()},WHITE);
        center(api,279,if m.artist_len==0 {"Connect a music source"} else {m.artist_str()},GREY);
        center(api,307,if !m.phone_available {"NO PHONE MEDIA"} else if m.playing {"NOW PLAYING"} else {"PAUSED"},ACCENT);
        center(api,329,status,GREY);
        for x in [126i16,204,282] { api.rect(x,365,58,48,PANEL); }
        center(api,382,"PREV     PLAY     NEXT",WHITE); center(api,430,"VOLUME N/A",GREY); api.show();
    }
    fn send_command(api:&Api,m:&Model,cmd:u8)->&'static str {
        if !m.phone_available { return "NO PHONE MEDIA"; }
        match api.media_key(cmd) { 0=>"COMMAND SENT", -19=>"NO PHONE MEDIA", -22=>"UPDATE LOADER", _=>"COMMAND FAILED" }
    }
    fn messages(api:&Api,m:&mut Model)->bool {
        let mut changed=false; let mut buf=[0u8;128];
        while let Some(p)=api.recv(&mut buf) { if m.apply(p).is_ok() { changed=true; } }
        changed
    }
    fn main(api:&'static Api)->i32 {
        let mut status=if api.media_bind(None,None)==0 {"WAITING FOR PHONE"} else {"MEDIA API UNAVAILABLE"};
        let m=unsafe { &mut *core::ptr::addr_of_mut!(MODEL) }; let mut dirty=true;
        loop {
            if dirty { draw(api,m,status); dirty=false; }
            let Some(e)=api.event(250) else { if messages(api,m) { dirty=true; } continue; };
            match e.kind {
                BUTTON => { let _=api.media_unbind(); return 0; }
                PAUSE => {}
                RESUME => dirty=true,
                MESSAGE => dirty |= messages(api,m),
                MEDIA => { m.phone_available=e.y!=0; m.playing=e.x!=0; status="PHONE STATE UPDATED"; dirty=true; }
                TOUCH_DOWN if e.y>=350 && e.y<=425 => {
                    status=if e.x<190 { send_command(api,m,MEDIA_PREV) }
                    else if e.x>276 { send_command(api,m,MEDIA_NEXT) }
                    else if m.playing { send_command(api,m,MEDIA_PAUSE) }
                    else { send_command(api,m,MEDIA_PLAY) };
                    dirty=true;
                }
                CROWN => { status="VOLUME NOT EXPOSED"; dirty=true; }
                _=>{}
            }
        }
    }
    static mut MODEL:Model=Model::new();
}
