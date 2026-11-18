#![cfg_attr(any(target_os = "none", wp2_sim), no_main, cfg_attr(target_os = "none", no_std))]

mod logic;

#[cfg(not(any(target_os = "none", wp2_sim)))]
fn main() {}

#[cfg(any(target_os = "none", wp2_sim))]
mod device {
    use crate::logic::{Model, ART_H, ART_W, MSG_MAX};
    use cmf_wp2_app::lv::{Align, Batch, Class, LongMode, Lv, Obj, Prop, ROOT};
    use cmf_wp2_app::{stock, Api, BUTTON, CROWN, MEDIA, MEDIA_NEXT, MEDIA_PAUSE, MEDIA_PLAY, MEDIA_PREV,
        MESSAGE, PAUSE, RESUME, TOUCH_DOWN};

    cmf_wp2_app::entry!(main);
    cmf_wp2_app::app_icon!();

    const BLACK: u16 = 0x0000;
    const WHITE: u16 = 0xFFFF;
    const DIM: u16 = 0x8410;
    const PANEL: u16 = 0x2124;
    const ACCENT: u16 = 0xFD20;
    const ART_X: i16 = 147;
    const ART_Y: i16 = 91;
    const PIXEL: i16 = 5;
    const REQUEST_MS: u32 = 4000;

    static mut MODEL: Model = Model::new();

    /// Transparent LVGL labels sit above the canvas: the loader fills them with the phone's live title and artist.
    struct Ui {
        lv: Lv,
        batch: Batch,
        title: Obj,
        artist: Obj,
    }

    impl Ui {
        fn new(api: &Api) -> Option<Self> {
            let lv = api.lv()?;
            let mut batch = Batch::new()?;
            stock::theme(&mut batch).ok()?;
            // Keep the root transparent so the app's RGB565 album-art canvas remains visible underneath.
            batch.style(ROOT, Prop::BgOpa, 0, 0).ok()?;

            let title = batch.create(Class::Label, ROOT).ok()?;
            batch.size(title, 400, 38).ok()?;
            batch.style(title, Prop::TextAlign, 0, 2).ok()?;
            batch.style(title, Prop::TextColor, 0, WHITE as i32).ok()?;
            batch.long_mode(title, LongMode::Dot).ok()?;
            batch.text(title, "Waiting for track").ok()?;
            batch.align(title, Align::Center, 0, 16).ok()?;

            let artist = batch.create(Class::Label, ROOT).ok()?;
            batch.size(artist, 400, 34).ok()?;
            batch.style(artist, Prop::TextAlign, 0, 2).ok()?;
            batch.style(artist, Prop::TextColor, 0, DIM as i32).ok()?;
            batch.long_mode(artist, LongMode::Dot).ok()?;
            batch.text(artist, "Connect phone media").ok()?;
            batch.align(artist, Align::Center, 0, 48).ok()?;

            batch.submit(&lv).ok()?;
            Some(Self { lv, batch, title, artist })
        }

        fn metadata(&mut self, model: &Model) {
            if model.metadata_received {
                let _ = self.batch.text(self.title, model.title())
                    .and_then(|_| self.batch.text(self.artist, model.artist()))
                    .and_then(|_| self.batch.submit(&self.lv));
            }
        }
    }

    fn center(api: &Api, y: i16, text: &str, color: u16) {
        let mut n = text.len().min(56);
        while !text.is_char_boundary(n) { n -= 1; }
        let shown = &text[..n];
        let chars = shown.chars().count().min(28);
        let end = if chars < shown.chars().count() {
            shown.char_indices().nth(chars).map_or(shown.len(), |(i, _)| i)
        } else {
            shown.len()
        };
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
            api.rect(ART_X, ART_Y, 160, 160, PANEL);
            for y in 0..160i32 {
                let dy = y - 80;
                let outer = isqrt((80 * 80 - dy * dy).max(0) as u32) as i32;
                api.rect(ART_X + (80 - outer) as i16, ART_Y + y as i16,
                    (outer * 2 + 1) as u16, 1, 0x4208);
                let ring = 28i32;
                if dy.abs() <= ring {
                    let inner = isqrt((ring * ring - dy * dy).max(0) as u32) as i32;
                    api.rect(ART_X + (80 - inner) as i16, ART_Y + y as i16,
                        (inner * 2 + 1) as u16, 1, ACCENT);
                }
                let hole = 7i32;
                if dy.abs() <= hole {
                    let inner = isqrt((hole * hole - dy * dy).max(0) as u32) as i32;
                    api.rect(ART_X + (64 - inner) as i16, ART_Y + y as i16,
                        (inner * 2 + 1) as u16, 1, BLACK);
                }
            }
            center(api, ART_Y + 170, "ALBUM ART NOT RECEIVED", DIM);
        }
    }

    fn draw(api: &Api, model: &Model, status: &str) {
        api.rect(0, 0, api.width, api.height, BLACK);
        center(api, 22, "NOTHING MUSIC", WHITE);
        center(api, 48, "NOW PLAYING", DIM);
        draw_art(api, model);

        let state = if !model.phone_available { "NO PHONE MEDIA" }
            else if model.playing { "NOW PLAYING" } else { "PAUSED" };
        center(api, 300, state, ACCENT);
        center(api, 322, status, DIM);

        for x in [92i16, 184, 276] {
            api.rect(x, 356, 86, 58, PANEL);
        }
        api.text(110, 378, "PREVIOUS", WHITE);
        api.text(205, 378, if model.playing { "PAUSE" } else { "PLAY" }, WHITE);
        api.text(298, 378, "NEXT", WHITE);
        center(api, 434, "CROWN / BUTTON: BACK", DIM);
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

    fn receive(api: &Api, model: &mut Model, ui: &mut Option<Ui>) -> bool {
        let mut changed = false;
        let mut buf = [0u8; MSG_MAX];
        while let Some(packet) = api.recv(&mut buf) {
            if model.apply(packet).is_ok() {
                if let Some(view) = ui.as_mut() { view.metadata(model); }
                changed = true;
            }
        }
        changed
    }

    fn main(api: &'static Api) -> i32 {
        let model = unsafe { &mut *core::ptr::addr_of_mut!(MODEL) };
        let mut ui = Ui::new(api);
        let bind = match ui.as_ref() {
            Some(view) => api.media_bind(Some(view.title), Some(view.artist)),
            None => -22,
        };
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
                if receive(api, model, &mut ui) { dirty = true; }
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
                MESSAGE => dirty |= receive(api, model, &mut ui),
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
