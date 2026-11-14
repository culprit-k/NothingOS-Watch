/*
 * Portable countdown timer for NothingOS Wear.
 * Time is supplied by the platform's monotonic millisecond clock.
 * SPDX-License-Identifier: Apache-2.0
 */
#ifndef NOTHINGOS_TIMER_SERVICE_H
#define NOTHINGOS_TIMER_SERVICE_H
#include <stdbool.h>
#include <stdint.h>

typedef enum {
    NOTHINGOS_TIMER_IDLE = 0,
    NOTHINGOS_TIMER_RUNNING,
    NOTHINGOS_TIMER_PAUSED,
    NOTHINGOS_TIMER_FINISHED
} nothingos_timer_state_t;

typedef struct {
    uint32_t duration_ms;
    uint32_t remaining_ms;
    uint32_t last_update_ms;
    nothingos_timer_state_t state;
} nothingos_timer_t;

void nothingos_timer_init(nothingos_timer_t *timer, uint32_t duration_ms);
bool nothingos_timer_set_duration(nothingos_timer_t *timer,
                                  uint32_t duration_ms);
bool nothingos_timer_start(nothingos_timer_t *timer, uint32_t now_ms);
bool nothingos_timer_pause(nothingos_timer_t *timer, uint32_t now_ms);
void nothingos_timer_reset(nothingos_timer_t *timer);
void nothingos_timer_update(nothingos_timer_t *timer, uint32_t now_ms);
uint32_t nothingos_timer_remaining(const nothingos_timer_t *timer);
#endif
