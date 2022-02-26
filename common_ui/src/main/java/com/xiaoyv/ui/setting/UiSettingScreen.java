package com.xiaoyv.ui.setting;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.xiaoyv.desktop.ui.R;

/**
 * UiSettingScreen
 *
 * @author why
 * @since 2020/11/29
 **/
public class UiSettingScreen extends LinearLayoutCompat {
    private final Context context;
    public String sharePreferenceName;

    public UiSettingScreen(@NonNull Context context) {
        this(context, null);
    }

    public UiSettingScreen(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UiSettingScreen(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        setOrientation(VERTICAL);
        if (attrs == null) {
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.UiSettingScreen);
        sharePreferenceName = array.getString(R.styleable.UiSettingScreen_ui_sp);
        array.recycle();
    }

    public String getSharePreferenceName() {
        return sharePreferenceName;
    }

    public void setSharePreferenceName(String sharePreferenceName) {
        this.sharePreferenceName = sharePreferenceName;
    }
}
