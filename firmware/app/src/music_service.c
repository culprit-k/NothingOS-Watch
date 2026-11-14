/*
 * Portable NothingOS Wear music service.
 * SPDX-License-Identifier: Apache-2.0
 */
#include <stddef.h>
#include <string.h>
#include "music_service.h"

static bool valid_mode(nothingos_music_mode_t mode)
{
    return mode >= NOTHINGOS_MUSIC_PHONE &&
           mode < NOTHINGOS_MUSIC_MODE_COUNT;
}

void nothingos_music_init(nothingos_music_service_t *service)
{
    if (service == NULL) {
        return;
    }
    memset(service, 0, sizeof(*service));
    service->selected_mode = NOTHINGOS_MUSIC_PHONE;
    service->state[NOTHINGOS_MUSIC_PHONE] = NOTHINGOS_MUSIC_UNAVAILABLE;
    service->state[NOTHINGOS_MUSIC_LOCAL] = NOTHINGOS_MUSIC_UNAVAILABLE;
}

/* A binding does not imply readiness; the backend must report that separately. */
bool nothingos_music_bind(nothingos_music_service_t *service,
                          nothingos_music_mode_t mode,
                          const nothingos_music_backend_t *backend)
{
    if (service == NULL || !valid_mode(mode) ||
        backend == NULL || backend->command == NULL) {
        return false;
    }
    service->backend[mode] = *backend;
    service->state[mode] = NOTHINGOS_MUSIC_DISCONNECTED;
    return true;
}

bool nothingos_music_set_state(nothingos_music_service_t *service,
                               nothingos_music_mode_t mode,
                               nothingos_music_state_t state)
{
    if (service == NULL || !valid_mode(mode) ||
        state < NOTHINGOS_MUSIC_UNAVAILABLE ||
        state > NOTHINGOS_MUSIC_ERROR) {
        return false;
    }
    if (state != NOTHINGOS_MUSIC_UNAVAILABLE &&
        service->backend[mode].command == NULL) {
        return false;
    }
    service->state[mode] = state;
    return true;
}

bool nothingos_music_select_mode(nothingos_music_service_t *service,
                                 nothingos_music_mode_t mode)
{
    if (service == NULL || !valid_mode(mode)) {
        return false;
    }
    service->selected_mode = mode;
    return true;
}

int nothingos_music_command(nothingos_music_service_t *service,
                            nothingos_music_command_t command,
                            uint8_t value)
{
    nothingos_music_mode_t mode;
    nothingos_music_state_t state;
    nothingos_music_backend_t *backend;
    int rc;

    if (service == NULL ||
        command < NOTHINGOS_MUSIC_PLAY_PAUSE ||
        command > NOTHINGOS_MUSIC_SET_VOLUME) {
        return NOTHINGOS_MUSIC_EINVAL;
    }
    mode = service->selected_mode;
    if (!valid_mode(mode)) {
        return NOTHINGOS_MUSIC_EINVAL;
    }
    backend = &service->backend[mode];
    state = service->state[mode];

    if (backend->command == NULL || state == NOTHINGOS_MUSIC_UNAVAILABLE) {
        return NOTHINGOS_MUSIC_ENOTSUP;
    }
    if (state != NOTHINGOS_MUSIC_READY &&
        state != NOTHINGOS_MUSIC_PLAYING &&
        state != NOTHINGOS_MUSIC_PAUSED) {
        return NOTHINGOS_MUSIC_ENOTREADY;
    }
    if (command == NOTHINGOS_MUSIC_SET_VOLUME && value > 15U) {
        return NOTHINGOS_MUSIC_EINVAL;
    }

    rc = backend->command(command, value, backend->context);
    if (rc == NOTHINGOS_MUSIC_OK &&
        command == NOTHINGOS_MUSIC_SET_VOLUME) {
        service->volume = value;
    }
    /* Playback state changes only when a real backend event reports it. */
    return rc;
}

nothingos_music_state_t nothingos_music_get_state(
    const nothingos_music_service_t *service, nothingos_music_mode_t mode)
{
    if (service == NULL || !valid_mode(mode)) {
        return NOTHINGOS_MUSIC_UNAVAILABLE;
    }
    return service->state[mode];
}
