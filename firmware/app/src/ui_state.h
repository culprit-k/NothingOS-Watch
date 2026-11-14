/*
 * Portable NothingOS Wear UI state model.
 * SPDX-License-Identifier: Apache-2.0
 */
#ifndef NOTHINGOS_UI_STATE_H
#define NOTHINGOS_UI_STATE_H

#include <stdbool.h>
#include <stdint.h>

typedef enum {
    NOTHINGOS_PAGE_HOME = 0,
    NOTHINGOS_PAGE_APPS = 1,
    NOTHINGOS_PAGE_SYSTEM = 2,
    NOTHINGOS_PAGE_COUNT = 3
} nothingos_page_t;

typedef struct {
    nothingos_page_t page;
    bool display_active;
    uint32_t uptime_seconds;
} nothingos_ui_state_t;

void nothingos_ui_state_init(nothingos_ui_state_t *state);
bool nothingos_ui_state_select(nothingos_ui_state_t *state,
                               nothingos_page_t page);
nothingos_page_t nothingos_ui_state_next(nothingos_ui_state_t *state);
void nothingos_ui_state_set_display(nothingos_ui_state_t *state, bool active);
void nothingos_ui_state_set_uptime(nothingos_ui_state_t *state,
                                   uint32_t seconds);

#endif
