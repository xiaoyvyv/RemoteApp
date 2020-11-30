package com.xiaoyv.main.splash;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.xiaoyv.busines.base.BaseActivity;
import com.xiaoyv.main.databinding.MainActivitySplashBinding;
import com.xiaoyv.main.home.HomeActivity;

/**
 * SplashActivity
 *
 * @author why
 * @since 2020/11/28
 **/
public class SplashActivity extends BaseActivity {
    private MainActivitySplashBinding binding;

    @Override
    protected View createContentView() {
        binding = MainActivitySplashBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }

    @Override
    protected void initListener() {
        binding.tvEnter.setOnClickListener(v -> {
            ActivityUtils.startActivity(HomeActivity.class);
            finish();
        });
    }
}
