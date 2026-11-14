/*
 * Host tests for the NothingOS Wear music service.
 * SPDX-License-Identifier: Apache-2.0
 */
#include <assert.h>
#include <stdio.h>
#include "music_service.h"

typedef struct {
    unsigned int calls;
    nothingos_music_command_t last_command;
    uint8_t last_value;
    int result;
} mock_backend_t;

static int mock_command(nothingos_music_command_t command, uint8_t value,
                        void *context)
{
    mock_backend_t *mock = (mock_backend_t *)context;
    mock->calls++;
    mock->last_command = command;
    mock->last_value = value;
    return mock->result;
}

static void test_defaults_are_unavailable(void)
{
    nothingos_music_service_t service;
    nothingos_music_init(&service);
    assert(service.selected_mode == NOTHINGOS_MUSIC_PHONE);
    assert(nothingos_music_get_state(&service, NOTHINGOS_MUSIC_PHONE) ==
           NOTHINGOS_MUSIC_UNAVAILABLE);
    assert(nothingos_music_get_state(&service, NOTHINGOS_MUSIC_LOCAL) ==
           NOTHINGOS_MUSIC_UNAVAILABLE);
    assert(nothingos_music_command(&service, NOTHINGOS_MUSIC_PLAY_PAUSE, 0U) ==
           NOTHINGOS_MUSIC_ENOTSUP);
}

static void test_modes_are_independent(void)
{
    nothingos_music_service_t service;
    mock_backend_t phone = {0};
    mock_backend_t local = {0};
    nothingos_music_backend_t phone_backend = {mock_command, &phone};
    nothingos_music_backend_t local_backend = {mock_command, &local};

    nothingos_music_init(&service);
    assert(nothingos_music_bind(&service, NOTHINGOS_MUSIC_PHONE, &phone_backend));
    assert(nothingos_music_bind(&service, NOTHINGOS_MUSIC_LOCAL, &local_backend));
    assert(nothingos_music_set_state(&service, NOTHINGOS_MUSIC_PHONE,
                                     NOTHINGOS_MUSIC_READY));
    assert(nothingos_music_set_state(&service, NOTHINGOS_MUSIC_LOCAL,
                                     NOTHINGOS_MUSIC_DISCONNECTED));
    assert(nothingos_music_command(&service, NOTHINGOS_MUSIC_NEXT, 0U) ==
           NOTHINGOS_MUSIC_OK);
    assert(phone.calls == 1U);
    assert(local.calls == 0U);

    assert(nothingos_music_select_mode(&service, NOTHINGOS_MUSIC_LOCAL));
    assert(nothingos_music_command(&service, NOTHINGOS_MUSIC_NEXT, 0U) ==
           NOTHINGOS_MUSIC_ENOTREADY);
    assert(local.calls == 0U);
}

static void test_volume_validation_and_backend_result(void)
{
    nothingos_music_service_t service;
    mock_backend_t phone = {0};
    nothingos_music_backend_t backend = {mock_command, &phone};

    nothingos_music_init(&service);
    assert(nothingos_music_bind(&service, NOTHINGOS_MUSIC_PHONE, &backend));
    assert(nothingos_music_set_state(&service, NOTHINGOS_MUSIC_PHONE,
                                     NOTHINGOS_MUSIC_READY));
    assert(nothingos_music_command(&service, NOTHINGOS_MUSIC_SET_VOLUME, 101U) ==
           NOTHINGOS_MUSIC_EINVAL);
    assert(phone.calls == 0U);

    phone.result = -77;
    assert(nothingos_music_command(&service, NOTHINGOS_MUSIC_SET_VOLUME, 70U) ==
           -77);
    assert(service.volume_percent == 0U);

    phone.result = NOTHINGOS_MUSIC_OK;
    assert(nothingos_music_command(&service, NOTHINGOS_MUSIC_SET_VOLUME, 70U) ==
           NOTHINGOS_MUSIC_OK);
    assert(service.volume_percent == 70U);
    assert(phone.last_command == NOTHINGOS_MUSIC_SET_VOLUME);
    assert(phone.last_value == 70U);
}

static void test_playback_state_requires_backend_event(void)
{
    nothingos_music_service_t service;
    mock_backend_t phone = {0};
    nothingos_music_backend_t backend = {mock_command, &phone};

    nothingos_music_init(&service);
    assert(nothingos_music_bind(&service, NOTHINGOS_MUSIC_PHONE, &backend));
    assert(nothingos_music_set_state(&service, NOTHINGOS_MUSIC_PHONE,
                                     NOTHINGOS_MUSIC_READY));
    assert(nothingos_music_command(&service, NOTHINGOS_MUSIC_PLAY_PAUSE, 0U) ==
           NOTHINGOS_MUSIC_OK);
    assert(nothingos_music_get_state(&service, NOTHINGOS_MUSIC_PHONE) ==
           NOTHINGOS_MUSIC_READY);
    assert(nothingos_music_set_state(&service, NOTHINGOS_MUSIC_PHONE,
                                     NOTHINGOS_MUSIC_PLAYING));
    assert(nothingos_music_get_state(&service, NOTHINGOS_MUSIC_PHONE) ==
           NOTHINGOS_MUSIC_PLAYING);
}

int main(void)
{
    test_defaults_are_unavailable();
    test_modes_are_independent();
    test_volume_validation_and_backend_result();
    test_playback_state_requires_backend_event();
    puts("NothingOS Wear music service tests: PASS");
    return 0;
}
