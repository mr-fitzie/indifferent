package com.synthtc.indifferent.ui;

import android.content.SharedPreferences;
import android.os.Bundle;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.synthtc.indifferent.R;
import com.synthtc.indifferent.util.Alarm;

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
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
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (KEY_ALARM_ENABLE.equals(key)) {
            if (sharedPreferences.getBoolean(KEY_ALARM_ENABLE, DEFAULT_ALARM_ENABLE)) {
                Alarm.set(getActivity());
            } else {
                Alarm.cancel(getActivity());
            }
        }
    }
}
