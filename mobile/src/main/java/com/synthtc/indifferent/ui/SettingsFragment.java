package com.synthtc.indifferent.ui;

import android.os.Bundle;
import android.preference.Preference;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.synthtc.indifferent.R;
import com.synthtc.indifferent.util.Alarm;

public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {
    // Keep the following values in sync with the preferences.xml
    public static String KEY_ALARM_ENABLE = "pref_alarm_enable";
    public static String KEY_ALARM_RETRY_MIN = "pref_alarm_retry";
    public static String KEY_ALARM_TEST = "pref_alarm_test";
    public static String DEFAULT_ALARM_RETRY_MIN = "10";
    public static boolean DEFAULT_ALARM_ENABLE = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        Preference enableAlarm = findPreference(KEY_ALARM_ENABLE);
        enableAlarm.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference.getKey().equals(KEY_ALARM_ENABLE)) {
            Boolean isChecked = (Boolean) newValue;
            if (isChecked) {
                Alarm.set(getActivity(), false);
                preference.setIcon(R.drawable.ic_alarm_on);
            } else {
                Alarm.cancel(getActivity(), false);
                preference.setIcon(R.drawable.ic_alarm_off);
            }
        }
        return true;
    }
}
