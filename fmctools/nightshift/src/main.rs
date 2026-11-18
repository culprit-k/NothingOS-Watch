#![cfg_attr(any(target_os = "none", wp2_sim), no_main, cfg_attr(target_os = "none", no_std))]

mod logic;

#[cfg(not(any(target_os = "none", wp2_sim)))]
fn main() {}

#[cfg(any(target_os = "none", wp2_sim))]
mod device {
    use crate::logic::{mmss, page_step, remaining, FOCUS_SECONDS, PAGE_COUNT};
    use cmf_wp2_app::{Api, BUTTON, CROWN, HEART, PAUSE, RESUME, TOUCH_DOWN};

    cmf_wp2_app::entry!(main);
    cmf_wp2_app::app_icon!();

    const BLACK: u16 = 0x0000;
    const WHITE: u16 = 0xFFFF;
    const DIM: u16 = 0x7BEF;
    const CYAN: u16 = 0x07FF;
    const PURPLE: u16 = 0xA01F;
    const PINK: u16 = 0xF81F;
    const GREEN: u16 = 0x07E0;
    const PANEL: u16 = 0x18E3;
    const HZ: u16 = 31;

    const SOS_DURATIONS: [u16; 18] = [
        120, 100, 120, 100, 120, 260,
        360, 120, 360, 120, 360, 260,
        120, 100, 120, 100, 120, 0,
    ];

    fn center(api: &Api, y: i16, text: &str, color: u16) {
        let n = text.len().min(28);
        api.text(api.width as i16 / 2 - 8 * n as i16, y, &text[..n], color);
    }

    fn digits2(v: u8, out: &mut [u8; 2]) {
        out[0] = b'0' + (v / 10) % 10;
        out[1] = b'0' + v % 10;
    }

    fn draw_clock(api: &Api, y: i16) {
        if let Some(t) = api.time() {
            let mut h = [0u8; 2];
            let mut m = [0u8; 2];
            let mut s = [0u8; 2];
            digits2(t.hour, &mut h);
            digits2(t.minute, &mut m);
            digits2(t.second, &mut s);
            let mut clock = [0u8; 8];
            clock[0..2].copy_from_slice(&h);
            clock[2] = b':';
            clock[3..5].copy_from_slice(&m);
            clock[5] = b':';
            clock[6..8].copy_from_slice(&s);
            if let Ok(v) = core::str::from_utf8(&clock) {
                center(api, y, v, WHITE);
            }
            let mut mo = [0u8; 2];
            let mut d = [0u8; 2];
            digits2(t.month, &mut mo);
            digits2(t.day, &mut d);
            let mut date = [0u8; 10];
            date[0..2].copy_from_slice(&d);
            date[2] = b'/';
            date[3..5].copy_from_slice(&mo);
            date[5] = b'/';
            let yr = t.year;
            date[6] = b'0' + ((yr / 1000) % 10) as u8;
            date[7] = b'0' + ((yr / 100) % 10) as u8;
            date[8] = b'0' + ((yr / 10) % 10) as u8;
            date[9] = b'0' + (yr % 10) as u8;
            if let Ok(v) = core::str::from_utf8(&date) {
                center(api, y + 34, v, DIM);
            }
        } else {
            center(api, y, "--:--:--", WHITE);
            center(api, y + 34, "WAITING FOR RTC", DIM);
        }
    }

    fn ring(api: &Api, cx: i16, cy: i16, radius: i16, thickness: i16, color: u16) {
        let r = radius as i32;
        let inner = (radius - thickness).max(0) as i32;
        for dy in -r..=r {
            let outer = isqrt((r * r - dy * dy).max(0) as u32) as i32;
            let iy = dy.abs();
            let inner_x = if iy <= inner { isqrt((inner * inner - iy * iy).max(0) as u32) as i32 } else { 0 };
            let x0 = cx - outer as i16;
            let width = (outer - inner_x) * 2 + if iy <= inner { 0 } else { 1 };
            if width > 0 {
                api.rect(x0, cy + dy as i16, width as u16, 1, color);
            }
        }
    }

    fn isqrt(n: u32) -> u32 {
        let (mut lo, mut hi) = (0u32, n.min(512) + 1);
        while lo + 1 < hi {
            let mid = lo + (hi - lo) / 2;
            if mid * mid <= n { lo = mid; } else { hi = mid; }
        }
        lo
    }

    fn header(api: &Api, name: &str, index: usize) {
        center(api, 28, "N I G H T S H I F T", CYAN);
        center(api, 62, name, WHITE);
        let start = 153i16;
        for i in 0..PAGE_COUNT {
            let color = if i == index { CYAN } else { PANEL };
            api.rect(start + i as i16 * 40, 90, 26, 4, color);
        }
    }

    fn footer(api: &Api, hint: &str) {
        center(api, 420, hint, DIM);
        center(api, 444, "CROWN: NAVIGATE", DIM);
    }

    fn draw(api: &Api, page: usize, bpm: u16, worn: bool, left: u32, running: bool, sos: bool) {
        api.rect(0, 0, api.width, api.height, BLACK);
        let name = match page {
            0 => "CORE / LOCAL TIME",
            1 => "PULSE / SENSOR",
            2 => "FOCUS / 25:00",
            3 => "SIGNAL / HAPTICS",
            _ => "SYSTEM / ABOUT",
        };
        header(api, name, page);
        match page {
            0 => {
                ring(api, 233, 220, 112, 3, PANEL);
                ring(api, 233, 220, 98, 2, 0x39E7);
                center(api, 186, "LOCAL TIME", DIM);
                draw_clock(api, 218);
                center(api, 322, "FIVE PANELS. ONE WRIST.", CYAN);
                center(api, 352, "TURN CROWN TO EXPLORE", DIM);
                footer(api, "TAP: HAPTIC PING");
            }
            1 => {
                ring(api, 233, 220, 108, 5, if worn { PINK } else { PANEL });
                center(api, 160, "HEART RATE", DIM);
                if bpm > 0 {
                    let mut b = [0u8; 3];
                    let n = if bpm >= 100 { 3 } else if bpm >= 10 { 2 } else { 1 };
                    let v = bpm.min(999);
                    for i in 0..n { b[n - 1 - i] = b'0' + ((v / 10u16.pow(i as u32)) % 10) as u8; }
                    if let Ok(s) = core::str::from_utf8(&b[..n]) { center(api, 205, s, WHITE); }
                    center(api, 258, "BPM", PINK);
                } else {
                    center(api, 210, "--", WHITE);
                    center(api, 258, "WAITING FOR SENSOR", DIM);
                }
                center(api, 310, if worn { "WRIST CONTACT" } else { "CHECK WRIST CONTACT" }, if worn { GREEN } else { DIM });
                footer(api, "LIVE SENSOR / NON-MEDICAL");
            }
            2 => {
                ring(api, 233, 220, 112, 6, PANEL);
                ring(api, 233, 220, 100, 3, PURPLE);
                let mut buf = [0u8; 5];
                center(api, 195, mmss(left, &mut buf), WHITE);
                center(api, 248, if running { "FOCUS ACTIVE" } else { "READY TO FOCUS" }, CYAN);
                center(api, 315, "TAP TOP: RESET", DIM);
                footer(api, "TAP BOTTOM: START / PAUSE");
            }
            3 => {
                ring(api, 233, 220, 104, 4, if sos { PINK } else { PANEL });
                center(api, 172, "MORSE / SOS", DIM);
                center(api, 216, "... --- ...", WHITE);
                center(api, 274, if sos { "TRANSMITTING" } else { "LOCAL VIBRATION DEMO" }, if sos { PINK } else { CYAN });
                center(api, 324, "NO MESSAGE IS SENT", DIM);
                footer(api, "TAP TO PLAY SOS PATTERN");
            }
            _ => {
                ring(api, 233, 220, 108, 4, CYAN);
                center(api, 164, "NIGHTSHIFT 0.1", WHITE);
                center(api, 208, "CMF WATCH PRO 2", CYAN);
                center(api, 240, "D398 / JX402 TARGET", DIM);
                center(api, 286, "RTC / HEART / HAPTICS", WHITE);
                center(api, 318, "NO GPS / AUDIO / WIFI API", DIM);
                footer(api, "CROWN PRESS: EXIT");
            }
        }
        api.show();
    }

    fn pause_until_resume(api: &Api) -> bool {
        api.vibrate(0, HZ);
        loop {
            match api.event(u32::MAX).map(|e| e.kind) {
                Some(RESUME) => return true,
                Some(BUTTON) => return false,
                _ => {}
            }
        }
    }

    fn main(api: &'static Api) -> i32 {
        let mut page = 0usize;
        let mut bpm = 0u16;
        let mut worn = false;
        let mut focus_running = false;
        let mut focus_deadline = 0u32;
        let mut focus_left = FOCUS_SECONDS;
        let mut sos_active = false;
        let mut sos_step = 0usize;
        let mut sos_due = 0u32;
        let mut last_draw = api.now().wrapping_sub(1000);
        let mut ping_due = 0u32;
        let mut focus_done_due = 0u32;
        let _ = api.heart_rate(true);

        loop {
            let now = api.now();
            if ping_due != 0 && (now.wrapping_sub(ping_due) as i32) >= 0 { let _ = api.vibrate(0, HZ); ping_due = 0; }
            if focus_done_due != 0 && (now.wrapping_sub(focus_done_due) as i32) >= 0 { let _ = api.vibrate(0, HZ); focus_done_due = 0; }
            if focus_running {
                focus_left = remaining(focus_deadline, now);
                if focus_left == 0 { focus_running = false; let _ = api.vibrate(650, HZ); focus_done_due = now.wrapping_add(220); }
            }
            if sos_active && (now.wrapping_sub(sos_due) as i32) >= 0 {
                if sos_step >= SOS_DURATIONS.len() {
                    sos_active = false;
                    let _ = api.vibrate(0, HZ);
                } else {
                    let duration = SOS_DURATIONS[sos_step] as u32;
                    let on = sos_step % 2 == 0 && duration > 0;
                    let _ = api.vibrate(if on { 650 } else { 0 }, HZ);
                    sos_due = now.wrapping_add(duration.max(1));
                    sos_step += 1;
                }
            }
            if (now.wrapping_sub(last_draw) as i32) >= 0 {
                draw(api, page, bpm, worn, focus_left, focus_running, sos_active);
                last_draw = now.wrapping_add(if page == 2 && focus_running { 250 } else { 1000 });
            }

            let mut timeout = 250u32;
            if sos_active {
                let until = sos_due.wrapping_sub(now) as i32;
                if until > 0 { timeout = timeout.min(until as u32); } else { timeout = 0; }
            }
            let Some(e) = api.event(timeout) else { continue; };
            match e.kind {
                BUTTON => {
                    let _ = api.heart_rate(false);
                    let _ = api.vibrate(0, HZ);
                    return 0;
                }
                PAUSE => {
                    if !pause_until_resume(api) {
                        let _ = api.heart_rate(false);
                        return 0;
                    }
                    last_draw = api.now();
                }
                RESUME => last_draw = api.now(),
                HEART => {
                    bpm = e.delta.max(0) as u16;
                    worn = e.x != 0;
                    last_draw = api.now();
                }
                CROWN => {
                    page = page_step(page, e.delta);
                    if page == 1 { let _ = api.heart_rate(true); }
                    else { let _ = api.heart_rate(false); }
                    last_draw = api.now();
                }
                TOUCH_DOWN => {
                    match page {
                        0 => { let _ = api.vibrate(250, HZ); ping_due = api.now().wrapping_add(25); }
                        1 => { let _ = api.heart_rate(true); }
                        2 if e.y < 300 => { focus_running = false; focus_left = FOCUS_SECONDS; }
                        2 => {
                            if focus_running { focus_left = remaining(focus_deadline, api.now()); focus_running = false; }
                            else if focus_left == 0 { focus_left = FOCUS_SECONDS; }
                            else { focus_deadline = api.now().wrapping_add(focus_left * 1000); focus_running = true; }
                        }
                        3 => { sos_active = true; sos_step = 0; sos_due = api.now(); }
                        _ => { page = page_step(page, 1); }
                    }
                    last_draw = api.now();
                }
                _ => {}
            }
        }
    }
}
