/*
 * Copyright (C) 2019-2021 ConquerOS Project
 *           (C) 2021-2025 Halcyon Project
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
package com.android.settings.halcyon.display;

import android.os.Bundle;
import android.content.Context;

import com.android.settings.dashboard.DashboardFragment;
import com.android.settings.R;

import com.android.internal.logging.nano.MetricsProto.MetricsEvent;
import com.android.settingslib.core.AbstractPreferenceController;

import java.util.ArrayList;
import java.util.List;

public class QuickSettingsPreferences extends DashboardFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getLogTag() {
        return "QuickSettingsPreferences";
    }

    @Override
    protected int getPreferenceScreenResId() {
        return R.xml.quicksettings_perferences;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsEvent.HALCYON;
    }

    @Override
    protected List<AbstractPreferenceController> createPreferenceControllers(Context context) {
        final List<AbstractPreferenceController> controllers = new ArrayList<>();
        controllers.add(new DisableWindowBlursPreferenceController(context));
        return controllers;
    }
}