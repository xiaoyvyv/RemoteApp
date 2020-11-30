package com.xiaoyv.ui.listener;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * SimpleTextChangeListener
 *
 * @author why
 * @since 2020/11/29
 **/
public abstract class SimpleTextChangeListener implements TextWatcher {

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        afterTextChanged(String.valueOf(s));
    }

    public abstract void afterTextChanged(String input);
}
