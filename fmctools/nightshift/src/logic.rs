//! Pure navigation and timer logic for NIGHTSHIFT.
pub const PAGE_COUNT: usize = 5;
pub const FOCUS_SECONDS: u32 = 25 * 60;

pub fn page_step(page: usize, delta: i16) -> usize {
    (page as i16 + delta).rem_euclid(PAGE_COUNT as i16) as usize
}

pub fn remaining(deadline: u32, now: u32) -> u32 {
    let d = deadline.wrapping_sub(now) as i32;
    if d <= 0 { 0 } else { (d as u32 + 999) / 1000 }
}

pub fn mmss(seconds: u32, out: &mut [u8; 5]) -> &str {
    let s = seconds.min(99 * 60 + 59);
    out[0] = b'0' + ((s / 60) / 10) as u8;
    out[1] = b'0' + ((s / 60) % 10) as u8;
    out[2] = b':';
    out[3] = b'0' + ((s % 60) / 10) as u8;
    out[4] = b'0' + (s % 10) as u8;
    core::str::from_utf8(out).unwrap_or("25:00")
}

#[cfg(test)]
mod tests {
    use super::*;
    #[test] fn pages_wrap() { assert_eq!(page_step(0, -1), 4); assert_eq!(page_step(4, 1), 0); }
    #[test] fn countdown_handles_expiry() { assert_eq!(remaining(10_000, 8_500), 2); assert_eq!(remaining(5_000, 5_000), 0); assert_eq!(remaining(2_000, 3_000), 0); }
    #[test] fn formats_timer() { let mut b = [0; 5]; assert_eq!(mmss(1500, &mut b), "25:00"); assert_eq!(mmss(9, &mut b), "00:09"); }
}
