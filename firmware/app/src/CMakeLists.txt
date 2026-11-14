# SPDX-License-Identifier: Apache-2.0
zephyr_include_directories(.)
target_sources(app PRIVATE
    main.c
    app_msg.c
    ui_state.c
    music_service.c
    timer_service.c
)
