package com.xiaoyv.rdp.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RecentlyNonNull;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.Utils;
import com.xiaoyv.busines.base.BaseActivity;
import com.xiaoyv.rdp.R;
import com.xiaoyv.rdp.databinding.RdpSettingAdvanceBinding;
import com.xiaoyv.rdp.databinding.RdpSettingDebugBinding;
import com.xiaoyv.rdp.databinding.RdpSettingPerformance3gBinding;
import com.xiaoyv.rdp.databinding.RdpSettingPerformanceBinding;
import com.xiaoyv.rdp.databinding.RdpSettingScreen3gBinding;
import com.xiaoyv.rdp.databinding.RdpSettingScreenBinding;
import com.xiaoyv.rdp.databinding.RdpSettingSecurityBinding;

/**
 * BookmarkSettingActivity
 *
 * @author why
 * @since 2020/12/06
 **/
public class BookmarkSettingActivity extends BaseActivity {
    public static String KEY_SETTING = "KEY_SETTING";
    public static String TYPE_SETTING_ADVANCE = "TYPE_SETTING_ADVANCE";
    public static String TYPE_SETTING_DEBUG = "TYPE_SETTING_DEBUG";
    public static String TYPE_SETTING_SCREEN = "TYPE_SETTING_SCREEN";
    public static String TYPE_SETTING_SCREEN3G = "TYPE_SETTING_SCREEN3G";
    public static String TYPE_SETTING_PERFORMANCE = "TYPE_SETTING_PERFORMANCE";
    public static String TYPE_SETTING_PERFORMANCE3G = "TYPE_SETTING_PERFORMANCE3G";
    private RdpSettingAdvanceBinding advanceBinding;
    private RdpSettingDebugBinding debugBinding;
    private RdpSettingScreenBinding screenBinding;
    private RdpSettingScreen3gBinding screen3gBinding;
    private RdpSettingPerformanceBinding performanceBinding;
    private RdpSettingPerformance3gBinding performance3gBinding;
    private String type;

    public static void openSelf(@NonNull String typeSetting) {
        Intent intent = new Intent(Utils.getApp(), BookmarkSettingActivity.class);
        intent.putExtra(KEY_SETTING, typeSetting);
        ActivityUtils.startActivity(intent);
    }


    @Override
    protected void initIntentData(@RecentlyNonNull Intent intent, @RecentlyNonNull Bundle bundle) {
        type = getIntent().getStringExtra(KEY_SETTING);
    }

    @Override
    protected View createContentView() {
        if (StringUtils.equals(TYPE_SETTING_ADVANCE, type)) {
            advanceBinding = RdpSettingAdvanceBinding.inflate(getLayoutInflater());
            return advanceBinding.getRoot();
        }
        if (StringUtils.equals(TYPE_SETTING_DEBUG, type)) {
            debugBinding = RdpSettingDebugBinding.inflate(getLayoutInflater());
            return debugBinding.getRoot();
        }
        if (StringUtils.equals(TYPE_SETTING_SCREEN, type)) {
            screenBinding = RdpSettingScreenBinding.inflate(getLayoutInflater());
            return screenBinding.getRoot();
        }
        if (StringUtils.equals(TYPE_SETTING_SCREEN3G, type)) {
            screen3gBinding = RdpSettingScreen3gBinding.inflate(getLayoutInflater());
            return screen3gBinding.getRoot();
        }
        if (StringUtils.equals(TYPE_SETTING_PERFORMANCE, type)) {
            performanceBinding = RdpSettingPerformanceBinding.inflate(getLayoutInflater());
            return performanceBinding.getRoot();
        }
        if (StringUtils.equals(TYPE_SETTING_PERFORMANCE3G, type)) {
            performance3gBinding = RdpSettingPerformance3gBinding.inflate(getLayoutInflater());
            return performance3gBinding.getRoot();
        }
        throw new IllegalStateException("Unexpected value: " + type);
    }

    @Override
    protected void initView() {
        if (StringUtils.equals(TYPE_SETTING_ADVANCE, type)) {
            advanceBinding.toolbar.setTitle(StringUtils.getString(R.string.settings_cat_advanced))
                    .setStartClickListener(v -> onBackPressed());
        }
        if (StringUtils.equals(TYPE_SETTING_DEBUG, type)) {
            debugBinding.toolbar.setTitle(StringUtils.getString(R.string.settings_debug))
                    .setStartClickListener(v -> onBackPressed());
        }
        if (StringUtils.equals(TYPE_SETTING_SCREEN, type)) {
            screenBinding.toolbar.setTitle(StringUtils.getString(R.string.settings_cat_screen))
                    .setStartClickListener(v -> onBackPressed());
        }
        if (StringUtils.equals(TYPE_SETTING_SCREEN3G, type)) {
            screen3gBinding.toolbar.setTitle(StringUtils.getString(R.string.settings_screen_3g))
                    .setStartClickListener(v -> onBackPressed());
        }
        if (StringUtils.equals(TYPE_SETTING_PERFORMANCE, type)) {
            performanceBinding.toolbar.setTitle(StringUtils.getString(R.string.settings_cat_performance))
                    .setStartClickListener(v -> onBackPressed());
        }
        if (StringUtils.equals(TYPE_SETTING_PERFORMANCE3G, type)) {
            performance3gBinding.toolbar.setTitle(StringUtils.getString(R.string.settings_performance_3g))
                    .setStartClickListener(v -> onBackPressed());
        }

    }


    @Override
    protected void initData() {
        if (StringUtils.equals(TYPE_SETTING_ADVANCE, type)) {
            initAdvance();
        }
    }

    private void initAdvance() {
        showGatewaySetting(advanceBinding.usvEnableGateway.isSwitch());
        advanceBinding.usvEnableGateway.setSwitchChangeListener((buttonView, isChecked) -> {
            showGatewaySetting(isChecked);
        });

        show3gSetting(advanceBinding.usvEnable3g.isSwitch());
        advanceBinding.usvEnable3g.setSwitchChangeListener((buttonView, isChecked) -> {
            show3gSetting(isChecked);
        });
    }


    public void show3gSetting(boolean show) {
        if (show) {
            advanceBinding.usvPerformance3g.setVisibility(View.VISIBLE);
            advanceBinding.usvScreen3g.setVisibility(View.VISIBLE);
        } else {
            advanceBinding.usvPerformance3g.setVisibility(View.GONE);
            advanceBinding.usvScreen3g.setVisibility(View.GONE);
        }
    }

    public void showGatewaySetting(boolean show) {
        if (show) {
            advanceBinding.usvGateway.setVisibility(View.VISIBLE);
        } else {
            advanceBinding.usvGateway.setVisibility(View.GONE);
        }
    }
}
