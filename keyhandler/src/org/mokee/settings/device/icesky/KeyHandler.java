/*
 * Copyright (C) 2018 The MoKee Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mokee.settings.device.icesky;

import android.content.Context;
import android.hardware.input.InputManager;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.os.Vibrator;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import com.android.internal.os.DeviceKeyHandler;

import mokee.providers.MKSettings;

public class KeyHandler implements DeviceKeyHandler {

    private static final String TAG = "KeyHandler";

    private static final int KEY_HOME = 172;

    private static final String TRUSTED_HOME_DEVICE_NAME = "qpnp_pon";

    private final int longPressTimeout = ViewConfiguration.getLongPressTimeout();

    private Context context;
    private Vibrator vibrator;

    private int trustedHomeDeviceId = 0;

    private boolean ongoingPowerLongPress = false;

    private Handler handler = new Handler(Looper.getMainLooper());

    public KeyHandler(Context context) {
        this.context = context;
        vibrator = context.getSystemService(Vibrator.class);
        if (vibrator == null || !vibrator.hasVibrator()) {
            vibrator = null;
        }
    }

    public KeyEvent handleKeyEvent(KeyEvent event) {
        if (handleHomeKeyEvent(event)) {
            return null;
        } else {
            return event;
        }
    }

    private boolean handleHomeKeyEvent(KeyEvent event) {
        if (trustedHomeDeviceId == 0) {
            final String deviceName = getDeviceName(event);
            if (TRUSTED_HOME_DEVICE_NAME.equals(deviceName)) {
                trustedHomeDeviceId = event.getDeviceId();
            } else {
                return false;
            }
        } else {
            if (trustedHomeDeviceId != event.getDeviceId()) {
                return false;
            }
        }

        if (event.getScanCode() != KEY_HOME) {
            return false;
        }

        switch (event.getAction()) {
            case KeyEvent.ACTION_DOWN:
                injectKey(KeyEvent.KEYCODE_HOME, KeyEvent.ACTION_DOWN, 0);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        injectKey(KeyEvent.KEYCODE_HOME, KeyEvent.ACTION_UP, KeyEvent.FLAG_CANCELED);
                        injectKey(KeyEvent.KEYCODE_POWER);
                        doHapticFeedback();
                    }
                }, longPressTimeout);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        injectKey(KeyEvent.KEYCODE_POWER, KeyEvent.ACTION_DOWN, 0);
                        doHapticFeedback();
                        ongoingPowerLongPress = true;
                    }
                }, 2000);
                break;
            case KeyEvent.ACTION_UP:
                if (ongoingPowerLongPress) {
                    injectKey(KeyEvent.KEYCODE_POWER, KeyEvent.ACTION_UP, 0);
                    ongoingPowerLongPress = false;
                } else {
                    injectKey(KeyEvent.KEYCODE_HOME, KeyEvent.ACTION_UP, 0);
                    handler.removeCallbacksAndMessages(null);
                }
                break;
        }

        return true;
    }

    private String getDeviceName(KeyEvent event) {
        final int deviceId = event.getDeviceId();
        final InputDevice device = InputDevice.getDevice(deviceId);
        return device == null ? null : device.getName();
    }

    private void injectKey(int code) {
        injectKey(code, KeyEvent.ACTION_DOWN, 0);
        injectKey(code, KeyEvent.ACTION_UP, 0);
    }

    private void injectKey(int code, int action, int flags) {
        final long now = SystemClock.uptimeMillis();
        InputManager.getInstance().injectInputEvent(new KeyEvent(
                        now, now, action, code, 0, 0,
                        KeyCharacterMap.VIRTUAL_KEYBOARD,
                        0, flags,
                        InputDevice.SOURCE_KEYBOARD),
                InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
    }

    private void doHapticFeedback() {
        if (vibrator == null) {
            return;
        }

        final boolean enabled = MKSettings.System.getInt(context.getContentResolver(),
                MKSettings.System.TOUCHSCREEN_GESTURE_HAPTIC_FEEDBACK, 1) != 0;
        if (enabled) {
            vibrator.vibrate(50);
        }
    }

}
