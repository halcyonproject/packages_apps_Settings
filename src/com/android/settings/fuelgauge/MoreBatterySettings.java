/*
 * Copyright (C) 2019-2021 The ConquerOS Project
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

package com.android.settings.fuelgauge;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.ListPreference;
import androidx.preference.SwitchPreferenceCompat;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceScreen;
import androidx.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import com.android.internal.logging.nano.MetricsProto;

import com.android.settings.halcyon.preference.SystemSettingListPreference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MoreBatterySettings extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String STATUS_BAR_BATTERY_STYLE = "status_bar_battery_style";
    private static final String STATUS_BAR_SHOW_BATTERY_PERCENT = "status_bar_battery_percent";
    private static final int STATUS_BAR_BATTERY_STYLE_TEXT = 2;

    private SystemSettingListPreference mStatusBarBatteryShowPercent;
    private PreferenceCategory mStatusBarBatteryCategory;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.halcyon_more_battery);
        PreferenceScreen prefSet = getPreferenceScreen();

        mStatusBarBatteryShowPercent = findPreference(STATUS_BAR_SHOW_BATTERY_PERCENT);
        SystemSettingListPreference statusBarBattery =
                findPreference(STATUS_BAR_BATTERY_STYLE);
        statusBarBattery.setOnPreferenceChangeListener(this);
        int value = Integer.parseInt(statusBarBattery.getValue());
        enableStatusBarBatteryDependents(value);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int value = Integer.parseInt((String) newValue);
        String key = preference.getKey();
        switch (key) {
            case STATUS_BAR_BATTERY_STYLE:
                enableStatusBarBatteryDependents(value);
                break;
        }
        return true;
    }

    private void enableStatusBarBatteryDependents(int batteryIconStyle) {
        mStatusBarBatteryShowPercent.setEnabled(batteryIconStyle != STATUS_BAR_BATTERY_STYLE_TEXT);

    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.HALCYON;
    }
}