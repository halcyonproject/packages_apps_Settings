package com.android.settings.display;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import androidx.preference.Preference;
import androidx.preference.EditTextPreference;
import com.android.settings.core.BasePreferenceController;

public class CustomCarrierPreferenceController extends BasePreferenceController
        implements Preference.OnPreferenceChangeListener {

    public CustomCarrierPreferenceController(Context context, String preferenceKey) {
        super(context, preferenceKey);
    }

    @Override
    public int getAvailabilityStatus() {
        return AVAILABLE;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String value = (String) newValue;
        Settings.System.putString(mContext.getContentResolver(), getPreferenceKey(), value);
        updateState(preference);
        return true;
    }

    @Override
    public void updateState(Preference preference) {
        super.updateState(preference);
        String value = Settings.System.getString(mContext.getContentResolver(), getPreferenceKey());
        if (!TextUtils.isEmpty(value)) {
            preference.setSummary(value);
            if (preference instanceof EditTextPreference) {
                ((EditTextPreference) preference).setText(value);
            }
        } else {
            if (Settings.System.LOCKSCREEN_SHOW_CUSTOM_CARRIER_TEXT_SIM1.equals(getPreferenceKey())) {
                preference.setSummary(com.android.settings.R.string.custom_carrier_sim1_summary);
            } else {
                preference.setSummary(com.android.settings.R.string.custom_carrier_sim2_summary);
            }
            if (preference instanceof EditTextPreference) {
                ((EditTextPreference) preference).setText("");
            }
        }
    }
}
