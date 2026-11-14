/*
 * Copyright (c) 2026 NothingOS Wear contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
#ifndef NOTHINGOS_APP_MSG_H
#define NOTHINGOS_APP_MSG_H

#include <msg_manager.h>

#define APP_NAME "main"

enum nothingos_message_type {
    MSG_UI = MSG_APP_MESSAGE_START,
};

enum nothingos_message_command {
    CMD_NULL = 0,
    CMD_SCREEN_OFF,
    CMD_SCREEN_ON,
    CMD_PAGE_NEXT,
};

int app_msg_init(void);
int app_msg_send(const char *receiver, uint8_t type, uint8_t cmd);

#endif
