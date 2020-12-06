package com.xiaoyv.rdp.setting;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.Utils;
import com.xiaoyv.busines.base.BaseActivity;
import com.xiaoyv.rdp.R;
import com.xiaoyv.rdp.databinding.RdpSettingClientBinding;
import com.xiaoyv.rdp.databinding.RdpSettingPowerBinding;
import com.xiaoyv.rdp.databinding.RdpSettingSecurityBinding;
import com.xiaoyv.rdp.databinding.RdpSettingUiBinding;

/**
 * UiSettingActivity
 *
 * @author why
 * @since 2020/12/04
 **/
public class AppSettingActivity extends BaseActivity {
    public static String KEY_SETTING = "KEY_SETTING";
    public static String TYPE_SETTING_UI = "TYPE_SETTING_UI";
    public static String TYPE_SETTING_SECURITY = "TYPE_SETTING_SECURITY";
    public static String TYPE_SETTING_POWER = "TYPE_SETTING_POWER";
    public static String TYPE_SETTING_CLIENT = "TYPE_SETTING_CLIENT";
    private String type;

    private RdpSettingUiBinding uiBinding;
    private RdpSettingSecurityBinding securityBinding;
    private RdpSettingPowerBinding powerBinding;
    private RdpSettingClientBinding clientBinding;

    public static void openSelf(@NonNull String typeSetting) {
        Intent intent = new Intent(Utils.getApp(), AppSettingActivity.class);
        intent.putExtra(KEY_SETTING, typeSetting);
        ActivityUtils.startActivity(intent);
    }

    @Override
    protected void initIntentData(Intent intent, Bundle bundle) {
        type = getIntent().getStringExtra(KEY_SETTING);
    }

    @Override
    protected View createContentView() {
        uiBinding = RdpSettingUiBinding.inflate(getLayoutInflater());
        securityBinding = RdpSettingSecurityBinding.inflate(getLayoutInflater());
        clientBinding = RdpSettingClientBinding.inflate(getLayoutInflater());
        powerBinding = RdpSettingPowerBinding.inflate(getLayoutInflater());

        if (StringUtils.equals(TYPE_SETTING_UI, type)) {
            return uiBinding.getRoot();
        }
        if (StringUtils.equals(TYPE_SETTING_POWER, type)) {
            return powerBinding.getRoot();
        }
        if (StringUtils.equals(TYPE_SETTING_SECURITY, type)) {
            return securityBinding.getRoot();
        }
        if (StringUtils.equals(TYPE_SETTING_CLIENT, type)) {
            return clientBinding.getRoot();
        }
        throw new IllegalStateException("Unexpected value: " + type);
    }

    @Override
    protected void initView() {
        if (StringUtils.equals(TYPE_SETTING_UI, type)) {
            uiBinding.toolbar.setTitle(StringUtils.getString(R.string.settings_cat_ui))
                    .setStartClickListener(v -> onBackPressed());
        }
        if (StringUtils.equals(TYPE_SETTING_POWER, type)) {
            powerBinding.toolbar.setTitle(StringUtils.getString(R.string.settings_cat_power))
                    .setStartClickListener(v -> onBackPressed());
        }
        if (StringUtils.equals(TYPE_SETTING_SECURITY, type)) {
            securityBinding.toolbar.setTitle(StringUtils.getString(R.string.settings_cat_security))
                    .setStartClickListener(v -> onBackPressed());
        }
        if (StringUtils.equals(TYPE_SETTING_CLIENT, type)) {
            clientBinding.toolbar.setTitle(StringUtils.getString(R.string.settings_cat_client))
                    .setStartClickListener(v -> onBackPressed());
        }
    }

    @Override
    protected void initData() {

    }
}
