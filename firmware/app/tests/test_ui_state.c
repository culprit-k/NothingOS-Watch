/*
 * Host tests for the NothingOS Wear UI state model.
 * SPDX-License-Identifier: Apache-2.0
 */
#include <assert.h>
#include <stdbool.h>
#include <stdio.h>
#include "ui_state.h"

static void test_initial_state(void)
{
    nothingos_ui_state_t state;
    nothingos_ui_state_init(&state);
    assert(state.page == NOTHINGOS_PAGE_HOME);
    assert(state.display_active);
    assert(state.uptime_seconds == 0U);
}

static void test_navigation_cycle(void)
{
    nothingos_ui_state_t state;
    nothingos_ui_state_init(&state);
    assert(nothingos_ui_state_next(&state) == NOTHINGOS_PAGE_APPS);
    assert(nothingos_ui_state_next(&state) == NOTHINGOS_PAGE_SYSTEM);
    assert(nothingos_ui_state_next(&state) == NOTHINGOS_PAGE_TIMER);
    assert(nothingos_ui_state_next(&state) == NOTHINGOS_PAGE_HOME);
}

static void test_select_page_and_reject_invalid(void)
{
    nothingos_ui_state_t state;
    nothingos_ui_state_init(&state);
    assert(nothingos_ui_state_select(&state, NOTHINGOS_PAGE_TIMER));
    assert(state.page == NOTHINGOS_PAGE_TIMER);
    assert(!nothingos_ui_state_select(&state, NOTHINGOS_PAGE_COUNT));
    assert(state.page == NOTHINGOS_PAGE_TIMER);
}

static void test_display_and_uptime(void)
{
    nothingos_ui_state_t state;
    nothingos_ui_state_init(&state);
    nothingos_ui_state_set_display(&state, false);
    nothingos_ui_state_set_uptime(&state, 1234U);
    assert(!state.display_active);
    assert(state.uptime_seconds == 1234U);
}

int main(void)
{
    test_initial_state();
    test_navigation_cycle();
    test_select_page_and_reject_invalid();
    test_display_and_uptime();
    puts("NothingOS Wear UI state tests: PASS");
    return 0;
}
