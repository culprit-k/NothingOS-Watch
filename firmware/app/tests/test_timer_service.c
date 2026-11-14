/*
 * Host tests for the NothingOS Wear countdown timer.
 * SPDX-License-Identifier: Apache-2.0
 */
#include <assert.h>
#include <stdint.h>
#include <stdio.h>
#include "timer_service.h"

static void test_initial_state_and_validation(void)
{
    nothingos_timer_t timer;
    nothingos_timer_init(&timer, 300000U);
    assert(timer.state == NOTHINGOS_TIMER_IDLE);
    assert(nothingos_timer_remaining(&timer) == 300000U);
    assert(!nothingos_timer_set_duration(&timer, 0U));
    assert(nothingos_timer_set_duration(&timer, 60000U));
    assert(nothingos_timer_remaining(&timer) == 60000U);
}

static void test_start_pause_resume(void)
{
    nothingos_timer_t timer;
    nothingos_timer_init(&timer, 10000U);
    assert(nothingos_timer_start(&timer, 1000U));
    nothingos_timer_update(&timer, 4000U);
    assert(nothingos_timer_remaining(&timer) == 7000U);
    assert(!nothingos_timer_set_duration(&timer, 5000U));
    assert(nothingos_timer_pause(&timer, 5000U));
    assert(timer.state == NOTHINGOS_TIMER_PAUSED);
    assert(nothingos_timer_remaining(&timer) == 6000U);
    assert(nothingos_timer_start(&timer, 9000U));
    nothingos_timer_update(&timer, 10000U);
    assert(nothingos_timer_remaining(&timer) == 5000U);
}

static void test_finish_and_reset(void)
{
    nothingos_timer_t timer;
    nothingos_timer_init(&timer, 2000U);
    assert(nothingos_timer_start(&timer, 100U));
    nothingos_timer_update(&timer, 2100U);
    assert(timer.state == NOTHINGOS_TIMER_FINISHED);
    assert(nothingos_timer_remaining(&timer) == 0U);
    assert(!nothingos_timer_start(&timer, 3000U));
    nothingos_timer_reset(&timer);
    assert(timer.state == NOTHINGOS_TIMER_IDLE);
    assert(nothingos_timer_remaining(&timer) == 2000U);
    assert(nothingos_timer_start(&timer, 5000U));
}

static void test_32_bit_uptime_wrap(void)
{
    nothingos_timer_t timer;
    nothingos_timer_init(&timer, 1000U);
    assert(nothingos_timer_start(&timer, UINT32_MAX - 199U));
    nothingos_timer_update(&timer, 100U);
    assert(nothingos_timer_remaining(&timer) == 700U);
}

static void test_pause_at_expiration(void)
{
    nothingos_timer_t timer;
    nothingos_timer_init(&timer, 1000U);
    assert(nothingos_timer_start(&timer, 0U));
    assert(!nothingos_timer_pause(&timer, 1000U));
    assert(timer.state == NOTHINGOS_TIMER_FINISHED);
}

int main(void)
{
    test_initial_state_and_validation();
    test_start_pause_resume();
    test_finish_and_reset();
    test_32_bit_uptime_wrap();
    test_pause_at_expiration();
    puts("NothingOS Wear timer service tests: PASS");
    return 0;
}
