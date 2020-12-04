package com.xiaoyv.rdp.setting;

import android.view.View;

import com.xiaoyv.busines.base.BaseActivity;
import com.xiaoyv.rdp.databinding.RdpSettingUiBinding;

/**
 * UiSettingActivity
 *
 * @author why
 * @since 2020/12/04
 **/
public class UiSettingActivity extends BaseActivity {
    private RdpSettingUiBinding binding;

    @Override
    protected View createContentView() {
        binding = RdpSettingUiBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }
}
