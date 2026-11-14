/*
 * Portable NothingOS Wear UI state model.
 * SPDX-License-Identifier: Apache-2.0
 */
#include "ui_state.h"

void nothingos_ui_state_init(nothingos_ui_state_t *state)
{
    if (state == NULL) {
        return;
    }
    state->page = NOTHINGOS_PAGE_HOME;
    state->display_active = true;
    state->uptime_seconds = 0U;
}

bool nothingos_ui_state_select(nothingos_ui_state_t *state,
                               nothingos_page_t page)
{
    if (state == NULL || page < NOTHINGOS_PAGE_HOME ||
        page >= NOTHINGOS_PAGE_COUNT) {
        return false;
    }
    state->page = page;
    return true;
}

nothingos_page_t nothingos_ui_state_next(nothingos_ui_state_t *state)
{
    if (state == NULL) {
        return NOTHINGOS_PAGE_HOME;
    }
    if (state->page < NOTHINGOS_PAGE_HOME ||
        state->page >= NOTHINGOS_PAGE_COUNT) {
        state->page = NOTHINGOS_PAGE_HOME;
    } else {
        state->page = (nothingos_page_t)(
            ((unsigned int)state->page + 1U) % (unsigned int)NOTHINGOS_PAGE_COUNT);
    }
    return state->page;
}

void nothingos_ui_state_set_display(nothingos_ui_state_t *state, bool active)
{
    if (state != NULL) {
        state->display_active = active;
    }
}

void nothingos_ui_state_set_uptime(nothingos_ui_state_t *state,
                                   uint32_t seconds)
{
    if (state != NULL) {
        state->uptime_seconds = seconds;
    }
}
