package com.synthtc.indifferent.util;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

/**
 * http://stackoverflow.com/a/16780074
 */
public class BroadcastPreference extends Preference implements Preference.OnPreferenceClickListener {
    public BroadcastPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        getContext().sendBroadcast(getIntent());
        return true;
    }
}
