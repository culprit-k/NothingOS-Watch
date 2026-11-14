/*
 * NothingOS Wear — original embedded UI application layer.
 *
 * Target family: Actions ATS3089C / Zephyr / LVGL.
 * This source intentionally does not assume D398 pin numbers, panel reset
 * timing, touch-controller wiring, or partition layout. Those belong in the
 * separately verified JX402 board port and must be derived from stock 1.0.0.73.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
#include <stdbool.h>
#include <stdint.h>
#include <stdio.h>
#include <string.h>

#include <board_cfg.h>
#include <os_common_api.h>
#include <lvgl/lvgl.h>
#include <lvgl/lvgl_ext.h>
#include <drivers/input/input_dev.h>
#include <mem_manager.h>
#include <sys_manager.h>
#include <thread_timer.h>
#include <sys_wakelock.h>

#include "app_msg.h"
#include "ui_state.h"

#define C_BG       0x070707
#define C_PANEL    0x171717
#define C_PANEL_2  0x222222
#define C_WHITE    0xF4F4F4
#define C_MUTED    0x929292
#define C_ACCENT   0xD7FF3F
#define C_RED      0xFF4D4D

static K_SEM_DEFINE(g_vsync_sem, 0, 1);
static nothingos_ui_state_t g_ui_state;
static lv_obj_t *g_uptime_label;

static void rebuild_page(void);

static void set_text_color(lv_obj_t *obj, uint32_t color)
{
    lv_obj_set_style_text_color(obj, lv_color_hex(color), 0);
}

static lv_obj_t *make_label(lv_obj_t *parent, const char *text,
                            uint32_t color, const lv_font_t *font)
{
    lv_obj_t *label = lv_label_create(parent);
    lv_label_set_text(label, text);
    set_text_color(label, color);
    if (font) {
        lv_obj_set_style_text_font(label, font, 0);
    }
    return label;
}

static void style_card(lv_obj_t *obj, uint32_t color)
{
    lv_obj_set_style_bg_color(obj, lv_color_hex(color), 0);
    lv_obj_set_style_bg_opa(obj, LV_OPA_COVER, 0);
    lv_obj_set_style_radius(obj, 18, 0);
    lv_obj_set_style_border_width(obj, 0, 0);
    lv_obj_set_style_pad_all(obj, 12, 0);
}

static void page_button_cb(lv_event_t *event)
{
    uintptr_t selected_page = (uintptr_t)lv_event_get_user_data(event);
    if (nothingos_ui_state_select(&g_ui_state,
            (nothingos_page_t)selected_page)) {
        rebuild_page();
    }
}

static void add_nav_button(lv_obj_t *parent, const char *text,
                           nothingos_page_t page)
{
    lv_obj_t *btn = lv_button_create(parent);
    style_card(btn, C_PANEL_2);
    lv_obj_set_size(btn, 112, 42);
    lv_obj_add_event_cb(btn, page_button_cb, LV_EVENT_CLICKED,
                        (void *)(uintptr_t)page);

    lv_obj_t *label = make_label(btn, text, C_WHITE, &lv_font_montserrat_14);
    lv_obj_center(label);
}

static void add_app_tile(lv_obj_t *parent, const char *name,
                         const char *state)
{
    lv_obj_t *tile = lv_obj_create(parent);
    style_card(tile, C_PANEL);
    lv_obj_set_size(tile, 166, 76);
    lv_obj_set_flex_flow(tile, LV_FLEX_FLOW_COLUMN);
    lv_obj_set_flex_align(tile, LV_FLEX_ALIGN_CENTER,
                          LV_FLEX_ALIGN_START, LV_FLEX_ALIGN_START);

    make_label(tile, name, C_WHITE, &lv_font_montserrat_14);
    make_label(tile, state, C_MUTED, NULL);
}

static void build_home(lv_obj_t *screen)
{
    lv_obj_t *brand = make_label(screen, "NOTHING OS", C_WHITE,
                                 &lv_font_montserrat_14);
    lv_obj_align(brand, LV_ALIGN_TOP_MID, 0, 30);

    lv_obj_t *time_panel = lv_obj_create(screen);
    style_card(time_panel, C_PANEL);
    lv_obj_set_size(time_panel, 310, 150);
    lv_obj_align(time_panel, LV_ALIGN_CENTER, 0, -5);

    lv_obj_t *sync = make_label(time_panel, "TIME SYNC REQUIRED",
                                C_ACCENT, &lv_font_montserrat_14);
    lv_obj_align(sync, LV_ALIGN_TOP_MID, 0, 18);

    lv_obj_t *hint = make_label(time_panel,
        "Connect the companion phone\nto set the clock.",
        C_WHITE, &lv_font_montserrat_14);
    lv_obj_set_style_text_align(hint, LV_TEXT_ALIGN_CENTER, 0);
    lv_obj_align(hint, LV_ALIGN_CENTER, 0, 18);

    lv_obj_t *status = make_label(screen, "PHONE LINK  ·  NOT CONNECTED",
                                  C_MUTED, &lv_font_montserrat_14);
    lv_obj_align(status, LV_ALIGN_BOTTOM_MID, 0, -56);

    g_uptime_label = make_label(screen, "SYSTEM UPTIME  00:00",
                                C_MUTED, NULL);
    lv_obj_align(g_uptime_label, LV_ALIGN_BOTTOM_MID, 0, -34);
}

static void build_apps(lv_obj_t *screen)
{
    lv_obj_t *title = make_label(screen, "APPS", C_WHITE,
                                 &lv_font_montserrat_24);
    lv_obj_align(title, LV_ALIGN_TOP_MID, 0, 28);

    lv_obj_t *grid = lv_obj_create(screen);
    lv_obj_set_size(grid, 350, 230);
    lv_obj_align(grid, LV_ALIGN_CENTER, 0, 4);
    lv_obj_set_style_bg_opa(grid, LV_OPA_TRANSP, 0);
    lv_obj_set_style_border_width(grid, 0, 0);
    lv_obj_set_style_pad_all(grid, 0, 0);
    lv_obj_set_layout(grid, LV_LAYOUT_FLEX);
    lv_obj_set_flex_flow(grid, LV_FLEX_FLOW_ROW_WRAP);
    lv_obj_set_flex_align(grid, LV_FLEX_ALIGN_CENTER,
                          LV_FLEX_ALIGN_CENTER, LV_FLEX_ALIGN_CENTER);

    add_app_tile(grid, "Timer", "Not integrated yet");
    add_app_tile(grid, "Media", "Phone link pending");
    add_app_tile(grid, "Health", "No sensor data");
    add_app_tile(grid, "Activity", "No sensor data");
}

static void build_settings(lv_obj_t *screen)
{
    lv_obj_t *title = make_label(screen, "SYSTEM", C_WHITE,
                                 &lv_font_montserrat_24);
    lv_obj_align(title, LV_ALIGN_TOP_MID, 0, 32);

    lv_obj_t *panel = lv_obj_create(screen);
    style_card(panel, C_PANEL);
    lv_obj_set_size(panel, 318, 206);
    lv_obj_align(panel, LV_ALIGN_CENTER, 0, 2);
    lv_obj_set_flex_flow(panel, LV_FLEX_FLOW_COLUMN);
    lv_obj_set_flex_align(panel, LV_FLEX_ALIGN_CENTER,
                          LV_FLEX_ALIGN_START, LV_FLEX_ALIGN_START);

    make_label(panel, "NothingOS Wear · alpha", C_WHITE,
               &lv_font_montserrat_14);
    make_label(panel, "Bluetooth: not integrated", C_MUTED, NULL);
    make_label(panel, "Sensors: not integrated", C_MUTED, NULL);
    make_label(panel, "OTA: disabled in development", C_RED, NULL);
    make_label(panel, "No user data is fabricated", C_ACCENT, NULL);
}

static void rebuild_page(void)
{
    lv_obj_t *screen = lv_screen_active();
    lv_obj_clean(screen);
    lv_obj_set_style_bg_color(screen, lv_color_hex(C_BG), 0);
    lv_obj_set_style_bg_opa(screen, LV_OPA_COVER, 0);
    g_uptime_label = NULL;

    switch (g_ui_state.page) {
    case NOTHINGOS_PAGE_HOME:
        build_home(screen);
        break;
    case NOTHINGOS_PAGE_APPS:
        build_apps(screen);
        break;
    case NOTHINGOS_PAGE_SYSTEM:
        build_settings(screen);
        break;
    default:
        nothingos_ui_state_select(&g_ui_state, NOTHINGOS_PAGE_HOME);
        build_home(screen);
        break;
    }

    lv_obj_t *nav = lv_obj_create(screen);
    lv_obj_set_size(nav, 350, 48);
    lv_obj_align(nav, LV_ALIGN_BOTTOM_MID, 0, -4);
    lv_obj_set_style_bg_opa(nav, LV_OPA_TRANSP, 0);
    lv_obj_set_style_border_width(nav, 0, 0);
    lv_obj_set_style_pad_all(nav, 0, 0);
    lv_obj_set_layout(nav, LV_LAYOUT_FLEX);
    lv_obj_set_flex_flow(nav, LV_FLEX_FLOW_ROW);
    lv_obj_set_flex_align(nav, LV_FLEX_ALIGN_SPACE_EVENLY,
                          LV_FLEX_ALIGN_CENTER, LV_FLEX_ALIGN_CENTER);
    add_nav_button(nav, "HOME", NOTHINGOS_PAGE_HOME);
    add_nav_button(nav, "APPS", NOTHINGOS_PAGE_APPS);
    add_nav_button(nav, "SYSTEM", NOTHINGOS_PAGE_SYSTEM);
}

static void display_vsync_cb(const lvx_display_observer_t *observer,
                             uint32_t timestamp)
{
    ARG_UNUSED(observer);
    ARG_UNUSED(timestamp);
    k_sem_give(&g_vsync_sem);
}

static void display_state_cb(const lvx_display_observer_t *observer,
                             uint32_t state)
{
    ARG_UNUSED(observer);
    if (state == LVX_DISPLAY_STATE_ON) {
        app_msg_send(APP_NAME, MSG_UI, CMD_SCREEN_ON);
    }
}

static const lvx_display_observer_t g_display_observer = {
    .vsync_cb = display_vsync_cb,
    .state_cb = display_state_cb,
};

static void key_callback(struct device *dev, struct input_value *value)
{
    ARG_UNUSED(dev);
    static uint32_t last_value;
    bool pressed = (value->keypad.value != 0 &&
                    value->keypad.value != last_value);
    last_value = value->keypad.value;

    if (pressed) {
        sys_wake_lock(FULL_WAKE_LOCK);
        sys_wake_unlock(FULL_WAKE_LOCK);
        app_msg_send(APP_NAME, MSG_UI, CMD_PAGE_NEXT);
    }
}

static void key_init(void)
{
    struct device *key = (struct device *)device_get_binding(
        CONFIG_INPUT_DEV_ACTS_ONOFF_KEY_NAME);
    if (!key) {
        SYS_LOG_WRN("NothingOS: crown/key input not available");
        return;
    }
    input_dev_enable(key);
    input_dev_register_notify(key, key_callback);
}

static void uptime_tick(struct thread_timer *timer, void *data)
{
    ARG_UNUSED(timer);
    ARG_UNUSED(data);
    nothingos_ui_state_set_uptime(&g_ui_state, k_uptime_get_32() / 1000U);

    if (g_ui_state.page == NOTHINGOS_PAGE_HOME && g_uptime_label) {
        lv_label_set_text_fmt(g_uptime_label, "SYSTEM UPTIME  %02u:%02u",
            (unsigned int)(g_ui_state.uptime_seconds / 60U),
            (unsigned int)(g_ui_state.uptime_seconds % 60U));
    }
}

static void process_ui_message(struct app_msg *msg)
{
    switch (msg->cmd) {
    case CMD_SCREEN_OFF:
        nothingos_ui_state_set_display(&g_ui_state, false);
        break;
    case CMD_SCREEN_ON:
        nothingos_ui_state_set_display(&g_ui_state, true);
        lv_obj_invalidate(lv_screen_active());
        break;
    case CMD_PAGE_NEXT:
        nothingos_ui_state_next(&g_ui_state);
        rebuild_page();
        break;
    default:
        break;
    }
}

int main(void)
{
    static struct thread_timer uptime_timer;

    nothingos_ui_state_init(&g_ui_state);
    mem_manager_init();
    system_init();
    app_msg_init();

    lvx_port_init();
    lvx_display_add_observer(&g_display_observer);
    lvx_refr_set_manual(NULL);

    rebuild_page();
    thread_timer_init(&uptime_timer, uptime_tick, NULL);
    thread_timer_start(&uptime_timer, 1000, 1000);

    key_init();
    system_ready();

    while (1) {
        struct app_msg msg;
        uint32_t timeout = MIN(lv_task_handler(),
                               thread_timer_next_timeout());
        int rc = poll_msg(&msg, &g_vsync_sem, timeout);

        if (rc == OS_POLL_MSG) {
            if (msg.type == MSG_UI) {
                process_ui_message(&msg);
            }
            if (msg.callback) {
                msg.callback(&msg, 0, NULL);
            }
        } else if (rc == OS_POLL_SEM && g_ui_state.display_active) {
            lvx_refr_all();
        }
        thread_timer_handle_expired();
    }

    return 0;
}
