package com.xiaoyv.ui.setting;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({UiSettingViewType.TYPE_STRING,
        UiSettingViewType.TYPE_BOOLEAN,
        UiSettingViewType.TYPE_INTEGER,
        UiSettingViewType.TYPE_FLOAT,
        UiSettingViewType.TYPE_LIST_STRING,
        UiSettingViewType.TYPE_LIST_INTEGER
})
@Retention(RetentionPolicy.SOURCE)
public @interface UiSettingViewType {
    /**
     * STRING
     */
    int TYPE_STRING = 1;
    /**
     * BOOLEAN
     */
    int TYPE_BOOLEAN = 2;
    /**
     * INTEGER
     */
    int TYPE_INTEGER = 3;
    /**
     * FLOAT
     */
    int TYPE_FLOAT = 4;
    /**
     * LIST_STRING
     */
    int TYPE_LIST_STRING = 5;
    /**
     * LIST_INTEGER
     */
    int TYPE_LIST_INTEGER = 6;
}