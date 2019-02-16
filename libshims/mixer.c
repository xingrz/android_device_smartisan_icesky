/* mixer.c
**
** Copyright 2011, The Android Open Source Project
**
** Redistribution and use in source and binary forms, with or without
** modification, are permitted provided that the following conditions are met:
**     * Redistributions of source code must retain the above copyright
**       notice, this list of conditions and the following disclaimer.
**     * Redistributions in binary form must reproduce the above copyright
**       notice, this list of conditions and the following disclaimer in the
**       documentation and/or other materials provided with the distribution.
**     * Neither the name of The Android Open Source Project nor the names of
**       its contributors may be used to endorse or promote products derived
**       from this software without specific prior written permission.
**
** THIS SOFTWARE IS PROVIDED BY The Android Open Source Project ``AS IS'' AND
** ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
** IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
** ARE DISCLAIMED. IN NO EVENT SHALL The Android Open Source Project BE LIABLE
** FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
** DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
** SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
** CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
** LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
** OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
** DAMAGE.
*/

#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <unistd.h>
#include <fcntl.h>
#include <errno.h>
#include <ctype.h>
#include <poll.h>

#include <sys/ioctl.h>

#include <linux/ioctl.h>
#define __force
#define __user
#include <sound/asound.h>

struct mixer {
    int fd;
};

/** Subscribes for the mixer events.
 * @param mixer A mixer handle.
 * @param subscribe value indicating subscribe or unsubscribe for events
 * @returns On success, zero.
 *  On failure, non-zero.
 * @ingroup libtinyalsa-mixer
 */
int mixer_subscribe_events(struct mixer *mixer, int subscribe)
{
    if (ioctl(mixer->fd, SNDRV_CTL_IOCTL_SUBSCRIBE_EVENTS, &subscribe) < 0) {
        return -1;
    }
    return 0;
}

/** Wait for mixer events.
 * @param mixer A mixer handle.
 * @param timeout timout value
 * @returns On success, 1.
 *  On failure, -errno.
 *  On timeout, 0
 * @ingroup libtinyalsa-mixer
 */
int mixer_wait_event(struct mixer *mixer, int timeout)
{
    struct pollfd pfd;

    pfd.fd = mixer->fd;
    pfd.events = POLLIN | POLLOUT | POLLERR | POLLNVAL;

    for (;;) {
        int err;
        err = poll(&pfd, 1, timeout);
        if (err < 0)
            return -errno;
        if (!err)
            return 0;
        if (pfd.revents & (POLLERR | POLLNVAL))
            return -EIO;
        if (pfd.revents & (POLLIN | POLLOUT))
            return 1;
    }
}

/** Consume a mixer event.
 * If mixer_subscribe_events has been called,
 * mixer_wait_event will identify when a control value has changed.
 * This function will clear a single event from the mixer so that
 * further events can be alerted.
 *
 * @param mixer A mixer handle.
 * @returns 0 on success.  -errno on failure.
 * @ingroup libtinyalsa-mixer
 */
int mixer_consume_event(struct mixer *mixer) {
    struct snd_ctl_event ev;
    ssize_t count = read(mixer->fd, &ev, sizeof(ev));
    // Exporting the actual event would require exposing snd_ctl_event
    // via the header file, and all associated structs.
    // The events generally tell you exactly which value changed,
    // but reading values you're interested isn't hard and simplifies
    // the interface greatly.
    return (count >= 0) ? 0 : -errno;
}
