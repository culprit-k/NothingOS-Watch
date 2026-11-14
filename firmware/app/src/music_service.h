/*
 * NothingOS Wear music service contract.
 *
 * This module deliberately contains no assumed hardware backend. Phone
 * control and local watch playback are separate modes and must be bound to
 * separately verified platform implementations.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
#ifndef NOTHINGOS_MUSIC_SERVICE_H
#define NOTHINGOS_MUSIC_SERVICE_H
#include <stdbool.h>
#include <stdint.h>
#define NOTHINGOS_MUSIC_VOLUME_MAX 100U

typedef enum {
    NOTHINGOS_MUSIC_PHONE = 0,
    NOTHINGOS_MUSIC_LOCAL = 1,
    NOTHINGOS_MUSIC_MODE_COUNT = 2
} nothingos_music_mode_t;
typedef enum {
    NOTHINGOS_MUSIC_UNAVAILABLE = 0,
    NOTHINGOS_MUSIC_DISCONNECTED,
    NOTHINGOS_MUSIC_READY,
    NOTHINGOS_MUSIC_PLAYING,
    NOTHINGOS_MUSIC_PAUSED,
    NOTHINGOS_MUSIC_ERROR
} nothingos_music_state_t;
typedef enum {
    NOTHINGOS_MUSIC_PLAY_PAUSE = 0,
    NOTHINGOS_MUSIC_NEXT,
    NOTHINGOS_MUSIC_PREVIOUS,
    NOTHINGOS_MUSIC_SET_VOLUME
} nothingos_music_command_t;
enum {
    NOTHINGOS_MUSIC_OK = 0,
    NOTHINGOS_MUSIC_EINVAL = -1,
    NOTHINGOS_MUSIC_ENOTSUP = -2,
    NOTHINGOS_MUSIC_ENOTREADY = -3
};
typedef int (*nothingos_music_command_fn)(
    nothingos_music_command_t command, uint8_t value, void *context);
typedef struct {
    nothingos_music_command_fn command;
    void *context;
} nothingos_music_backend_t;
typedef struct {
    nothingos_music_backend_t backend[NOTHINGOS_MUSIC_MODE_COUNT];
    nothingos_music_state_t state[NOTHINGOS_MUSIC_MODE_COUNT];
    nothingos_music_mode_t selected_mode;
    uint8_t volume_percent;
} nothingos_music_service_t;

void nothingos_music_init(nothingos_music_service_t *service);
bool nothingos_music_bind(nothingos_music_service_t *service,
                          nothingos_music_mode_t mode,
                          const nothingos_music_backend_t *backend);
bool nothingos_music_set_state(nothingos_music_service_t *service,
                               nothingos_music_mode_t mode,
                               nothingos_music_state_t state);
bool nothingos_music_select_mode(nothingos_music_service_t *service,
                                 nothingos_music_mode_t mode);
int nothingos_music_command(nothingos_music_service_t *service,
                            nothingos_music_command_t command,
                            uint8_t value);
nothingos_music_state_t nothingos_music_get_state(
    const nothingos_music_service_t *service, nothingos_music_mode_t mode);
#endif
