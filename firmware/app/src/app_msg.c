/*
 * Copyright (c) 2026 NothingOS Wear contributors.
 * SPDX-License-Identifier: Apache-2.0
 */
#include <os_common_api.h>
#include "app_msg.h"

int app_msg_init(void)
{
    const char *name = k_thread_name_get(os_current_get());
    return msg_manager_add_listener((char *)name, os_current_get()) ? 0 : -1;
}

int app_msg_send(const char *receiver, uint8_t type, uint8_t cmd)
{
    struct app_msg msg = {0};
    msg.type = type;
    msg.cmd = cmd;
    return send_async_msg((char *)receiver, &msg) ? 0 : -1;
}
