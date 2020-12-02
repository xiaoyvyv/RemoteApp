package com.freerdp.freerdpcore.utils;

import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;

public class IntListPreference extends ListPreference {

    public IntListPreference(Context context) {
        super(context);
    }

    public IntListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected String getPersistedString(String defaultReturnValue) {
        return String.valueOf(getPersistedInt(-1));
    }

    @Override
    protected boolean persistString(String value) {
        return persistInt(Integer.parseInt(value));
    }
}
