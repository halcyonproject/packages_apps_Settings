/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.android.settings.display;

import android.content.Context;
import android.hardware.display.ColorDisplayManager;
import android.text.TextUtils;

import androidx.preference.PreferenceScreen;

import com.android.settings.core.SliderPreferenceController;
import com.android.settings.widget.SeekBarPreference;

public class ColorBalancePreferenceController extends SliderPreferenceController {

    private final ColorDisplayManager mColorDisplayManager;
    private final int mChannel;

    public ColorBalancePreferenceController(Context context, String key) {
        super(context, key);
        mColorDisplayManager = context.getSystemService(ColorDisplayManager.class);
        mChannel = keyToChannel(key);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean isSliceable() {
        return TextUtils.equals(getPreferenceKey(), channelToKey(mChannel));
    }

    @Override
    public boolean isPublicSlice() {
        return true;
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        final SeekBarPreference preference = screen.findPreference(getPreferenceKey());
        preference.setContinuousUpdates(true);
        preference.setMax(getMax());
        preference.setMin(getMin());
        preference.setProgress(getSliderPosition());
    }

    @Override
    public int getSliderPosition() {
        return mColorDisplayManager.getColorBalanceChannel(mChannel);
    }

    @Override
    public boolean setSliderPosition(int position) {
        return mColorDisplayManager.setColorBalanceChannel(mChannel, position);
    }

    @Override
    public int getMax() {
        switch (mChannel) {
            case 3: // Hue: 0-360 degrees, default 180 (0 shift)
                return 360;
            case 4: // Saturation: 0-200%, default 100%
                return 200;
            case 5: // Contrast: 50-150%, default 100%
                return 150;
            default: // 8-bpc linear sRGB
                return 255;
        }
    }

    @Override
    public int getMin() {
        switch (mChannel) {
            case 3: // Hue
                return 0;
            case 4: // Saturation
                return 0;
            case 5: // Contrast (limit to 50% to prevent completely flat image)
                return 50;
            default: // Users shouldn't be able to (accidentally) make their display black. Limit it to 10%.
                return 25;
        }
    }

    private static int keyToChannel(String key) {
        switch (key) {
            case "color_balance_red":
                return 0;
            case "color_balance_green":
                return 1;
            case "color_balance_blue":
                return 2;
            case "color_balance_hue":
                return 3;
            case "color_balance_saturation":
                return 4;
            case "color_balance_contrast":
                return 5;
            default:
                throw new IllegalArgumentException("Unknown key: " + key);
        }
    }

    /* package-private */ static String channelToKey(int channel) {
        switch (channel) {
            case 0:
                return "color_balance_red";
            case 1:
                return "color_balance_green";
            case 2:
                return "color_balance_blue";
            case 3:
                return "color_balance_hue";
            case 4:
                return "color_balance_saturation";
            case 5:
                return "color_balance_contrast";
            default:
                throw new IllegalArgumentException("Unknown channel: " + channel);
        }
    }

    public static int getDefaultValue(int channel) {
        switch (channel) {
            case 0:
            case 1:
            case 2:
                return 255;
            case 3:
                return 180;
            case 4:
            case 5:
                return 100;
            default:
                throw new IllegalArgumentException("Unknown channel: " + channel);
        }
    }
}
