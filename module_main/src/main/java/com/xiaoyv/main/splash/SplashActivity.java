package com.xiaoyv.main.splash;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ActivityUtils;
import com.xiaoyv.blueprint.base.BaseActivity;
import com.xiaoyv.main.databinding.MainActivitySplashBinding;
import com.xiaoyv.main.home.HomeActivity;

/**
 * SplashActivity
 *
 * @author why
 * @since 2020/11/28
 **/
public class SplashActivity extends AppCompatActivity {
    private MainActivitySplashBinding binding;

    @Override
    protected void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = MainActivitySplashBinding.inflate(LayoutInflater.from(this));
        setContentView(binding.getRoot());
    }

}
