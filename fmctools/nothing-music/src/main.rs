#![cfg_attr(any(target_os = "none", wp2_sim), no_main, cfg_attr(target_os = "none", no_std))]

mod logic;

#[cfg(not(any(target_os = "none", wp2_sim)))]
fn main() {}

#[cfg(any(target_os = "none", wp2_sim))]
mod device {
    use crate::logic::{Model, ART_H, ART_W, MSG_MAX};
    use cmf_wp2_app::{
        Api, BUTTON, CROWN, MEDIA, MEDIA_NEXT, MEDIA_PAUSE, MEDIA_PLAY, MEDIA_PREV,
        MESSAGE, PAUSE, RESUME, TOUCH_DOWN,
    };

    cmf_wp2_app::entry!(main);
    cmf_wp2_app::app_icon!();

    const BLACK: u16 = 0x0000;
    const WHITE: u16 = 0xFFFF;
    const DIM: u16 = 0x8410;
    const PANEL: u16 = 0x2124;
    const ACCENT: u16 = 0xFD20;
    const ART_X: i16 = 169;
    const ART_Y: i16 = 83;
    const PIXEL: i16 = 4;
    const REQUEST_MS: u32 = 4000;

    static mut MODEL: Model = Model::new();

    fn center(api: &Api, y: i16, text: &str, color: u16) {
        let mut n = text.len().min(56);
        while !text.is_char_boundary(n) { n -= 1; }
        let shown = &text[..n];
        let chars = shown.chars().count().min(28);
        let mut end = shown.len();
        if chars < shown.chars().count() {
            end = shown.char_indices().nth(chars).map_or(shown.len(), |(i, _)| i);
        }
        let shown = &shown[..end];
        api.text(api.width as i16 / 2 - 8 * shown.chars().count() as i16, y, shown, color);
    }

    fn isqrt(n: u32) -> u32 {
        let (mut lo, mut hi) = (0u32, n.min(128) + 1);
        while lo + 1 < hi {
            let mid = lo + (hi - lo) / 2;
            if mid * mid <= n { lo = mid; } else { hi = mid; }
        }
        lo
    }

    fn draw_art(api: &Api, model: &Model) {
        if model.art_ready {
            for y in 0..ART_H {
                for x in 0..ART_W {
                    let i = (y * ART_W + x) * 2;
                    let color = u16::from_le_bytes([model.art[i], model.art[i + 1]]);
                    api.rect(ART_X + x as i16 * PIXEL, ART_Y + y as i16 * PIXEL,
                        PIXEL as u16, PIXEL as u16, color);
                }
            }
        } else {
            // Deliberate fallback artwork: a vinyl disc, not falsely presented as the real album cover.
            api.rect(ART_X, ART_Y, 128, 128, PANEL);
            for y in 0..128i32 {
                let dy = y - 64;
                let outer = isqrt((64 * 64 - dy * dy).max(0) as u32) as i32;
                api.rect(ART_X + (64 - outer) as i16, ART_Y + y as i16,
                    (outer * 2 + 1) as u16, 1, 0x4208);
                let ring = 28i32;
                if dy.abs() <= ring {
                    let inner = isqrt((ring * ring - dy * dy).max(0) as u32) as i32;
                    api.rect(ART_X + (64 - inner) as i16, ART_Y + y as i16,
                        (inner * 2 + 1) as u16, 1, ACCENT);
                }
                let hole = 7i32;
                if dy.abs() <= hole {
                    let inner = isqrt((hole * hole - dy * dy).max(0) as u32) as i32;
                    api.rect(ART_X + (64 - inner) as i16, ART_Y + y as i16,
                        (inner * 2 + 1) as u16, 1, BLACK);
                }
            }
            center(api, ART_Y + 140, "WAITING FOR ALBUM ART", DIM);
        }
    }

    fn draw(api: &Api, model: &Model, status: &str) {
        api.rect(0, 0, api.width, api.height, BLACK);
        center(api, 28, "NOTHING MUSIC", WHITE);
        center(api, 52, "NOW PLAYING", DIM);
        draw_art(api, model);

        center(api, 250, if model.title_len == 0 { "Waiting for track info" } else { model.title() }, WHITE);
        center(api, 276, if model.artist_len == 0 { "Connect phone media" } else { model.artist() }, DIM);

        let state = if !model.phone_available { "NO PHONE MEDIA" }
            else if model.playing { "NOW PLAYING" } else { "PAUSED" };
        center(api, 307, state, ACCENT);
        center(api, 329, status, DIM);

        for x in [112i16, 196, 280] {
            api.rect(x, 360, 74, 52, PANEL);
        }
        api.text(126, 378, "PREV", WHITE);
        api.text(211, 378, "PLAY", WHITE);
        api.text(294, 378, "NEXT", WHITE);
        center(api, 430, "CROWN: BACK", DIM);
        api.show();
    }

    fn paused(api: &Api) -> bool {
        loop {
            match api.event(u32::MAX).map(|e| e.kind) {
                Some(RESUME) => return true,
                Some(BUTTON) => return false,
                _ => {}
            }
        }
    }

    fn request_snapshot(api: &Api) {
        let _ = api.send(b"NM1R");
    }

    fn command(api: &Api, model: &Model, cmd: u8) -> &'static str {
        if !model.phone_available { return "NO PHONE MEDIA"; }
        match api.media_key(cmd) {
            0 => "COMMAND SENT",
            -19 => "NO PHONE MEDIA",
            -22 => "UPDATE APP LOADER",
            _ => "COMMAND FAILED",
        }
    }

    fn receive(api: &Api, model: &mut Model) -> bool {
        let mut changed = false;
        let mut buf = [0u8; MSG_MAX];
        while let Some(packet) = api.recv(&mut buf) {
            if model.apply(packet).is_ok() { changed = true; }
        }
        changed
    }

    fn main(api: &'static Api) -> i32 {
        let model = unsafe { &mut *core::ptr::addr_of_mut!(MODEL) };
        let bind = api.media_bind(None, None);
        let mut status = if bind == 0 { "WAITING FOR PHONE" }
            else if bind == -22 { "UPDATE APP LOADER" } else { "PHONE STATUS UNAVAILABLE" };
        let mut last_request = api.now().wrapping_sub(REQUEST_MS);
        let mut dirty = true;

        loop {
            let now = api.now();
            if now.wrapping_sub(last_request) >= REQUEST_MS {
                request_snapshot(api);
                last_request = now;
            }
            if dirty {
                draw(api, model, status);
                dirty = false;
            }
            let Some(event) = api.event(250) else {
                if receive(api, model) { dirty = true; }
                continue;
            };
            match event.kind {
                BUTTON => {
                    let _ = api.media_unbind();
                    return 0;
                }
                PAUSE => {
                    if !paused(api) {
                        let _ = api.media_unbind();
                        return 0;
                    }
                    request_snapshot(api);
                    dirty = true;
                }
                RESUME => {
                    request_snapshot(api);
                    dirty = true;
                }
                MESSAGE => dirty |= receive(api, model),
                MEDIA => {
                    model.set_media_state(event.x != 0, event.y != 0);
                    status = "PHONE STATE UPDATED";
                    dirty = true;
                }
                TOUCH_DOWN if event.y >= 350 && event.y <= 425 => {
                    status = if event.x < 190 { command(api, model, MEDIA_PREV) }
                        else if event.x > 276 { command(api, model, MEDIA_NEXT) }
                        else if model.playing { command(api, model, MEDIA_PAUSE) }
                        else { command(api, model, MEDIA_PLAY) };
                    dirty = true;
                }
                CROWN => {
                    status = "VOLUME: USE PHONE";
                    dirty = true;
                }
                _ => {}
            }
        }
    }
}
