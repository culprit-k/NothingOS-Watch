/*
 * Portable countdown timer for NothingOS Wear.
 * SPDX-License-Identifier: Apache-2.0
 */
#include <stddef.h>
#include "timer_service.h"

void nothingos_timer_init(nothingos_timer_t *timer, uint32_t duration_ms)
{
    if (timer == NULL) {
        return;
    }
    timer->duration_ms = duration_ms;
    timer->remaining_ms = duration_ms;
    timer->last_update_ms = 0U;
    timer->state = NOTHINGOS_TIMER_IDLE;
}

bool nothingos_timer_set_duration(nothingos_timer_t *timer,
                                  uint32_t duration_ms)
{
    if (timer == NULL || duration_ms == 0U ||
        timer->state == NOTHINGOS_TIMER_RUNNING) {
        return false;
    }
    timer->duration_ms = duration_ms;
    timer->remaining_ms = duration_ms;
    timer->last_update_ms = 0U;
    timer->state = NOTHINGOS_TIMER_IDLE;
    return true;
}

void nothingos_timer_update(nothingos_timer_t *timer, uint32_t now_ms)
{
    uint32_t elapsed;

    if (timer == NULL || timer->state != NOTHINGOS_TIMER_RUNNING) {
        return;
    }

    /* Unsigned subtraction deliberately tolerates the 32-bit uptime wrap. */
    elapsed = now_ms - timer->last_update_ms;
    timer->last_update_ms = now_ms;

    if (elapsed >= timer->remaining_ms) {
        timer->remaining_ms = 0U;
        timer->state = NOTHINGOS_TIMER_FINISHED;
    } else {
        timer->remaining_ms -= elapsed;
    }
}

bool nothingos_timer_start(nothingos_timer_t *timer, uint32_t now_ms)
{
    if (timer == NULL ||
        (timer->state != NOTHINGOS_TIMER_IDLE &&
         timer->state != NOTHINGOS_TIMER_PAUSED) ||
        timer->remaining_ms == 0U) {
        return false;
    }
    timer->last_update_ms = now_ms;
    timer->state = NOTHINGOS_TIMER_RUNNING;
    return true;
}

bool nothingos_timer_pause(nothingos_timer_t *timer, uint32_t now_ms)
{
    if (timer == NULL || timer->state != NOTHINGOS_TIMER_RUNNING) {
        return false;
    }
    nothingos_timer_update(timer, now_ms);
    if (timer->state != NOTHINGOS_TIMER_RUNNING) {
        return false;
    }
    timer->state = NOTHINGOS_TIMER_PAUSED;
    return true;
}

void nothingos_timer_reset(nothingos_timer_t *timer)
{
    if (timer == NULL) {
        return;
    }
    timer->remaining_ms = timer->duration_ms;
    timer->last_update_ms = 0U;
    timer->state = NOTHINGOS_TIMER_IDLE;
}

uint32_t nothingos_timer_remaining(const nothingos_timer_t *timer)
{
    return timer == NULL ? 0U : timer->remaining_ms;
}
