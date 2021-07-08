package com.xiaoyv.busines.base;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.FragmentUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.SnackbarUtils;
import com.xiaoyv.busines.BaseApp;
import com.xiaoyv.business.databinding.BusinessActivityRootBinding;
import com.xiaoyv.ui.status.ContentStatusView;

import java.util.List;

import me.jessyan.autosize.AutoSizeCompat;

/**
 * BaseActivity
 *
 * @author why
 * @since 2020/11/28
 **/
public abstract class BaseActivity extends AppCompatActivity implements IBaseView {
    protected BusinessActivityRootBinding rootBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBar();
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            initIntentData(getIntent(), bundle);
        }
        rootBinding = BusinessActivityRootBinding.inflate(getLayoutInflater());
        rootBinding.flRoot.addView(createContentView());
        setContentView(rootBinding.getRoot());
        initView();
        initEvent();
    }

    protected void initBar() {
        ScreenUtils.setPortrait(this);
        BarUtils.transparentStatusBar(this);
        BarUtils.setStatusBarLightMode(this, true);
    }

    /**
     * 导入视图
     *
     * @return 视图
     */
    protected abstract View createContentView();

    protected void initIntentData(@NonNull Intent intent, @NonNull Bundle bundle) {
    }

    /**
     * 初始化
     */
    protected abstract void initView();

    protected void initEvent() {
        initData();
        initListener();
    }

    /**
     * 初始化数据
     */
    protected abstract void initData();

    protected void initListener() {

    }

    @Override
    public Resources getResources() {
        Resources resources = super.getResources();
        // 解决 AutoSize 横屏时对话框显示状态，切后台再切回前台导致的适配失效问题
        runOnUiThread(() -> AutoSizeCompat.autoConvertDensity(resources, BaseApp.MAX_WIDTH_DP, !ScreenUtils.isLandscape()));
        return super.getResources();
    }

    @Override
    public void p2vShowToast(String msg) {
        SnackbarUtils.with(rootBinding.getRoot())
                .setMessage(msg)
                .show();
    }

    @Override
    public void p2vShowLoading() {
        LogUtils.v("p2vShowLoading");
    }

    @Override
    public void p2vShowLoading(String msg) {
        LogUtils.v("p2vShowLoading:" + msg);
    }

    @Override
    public void p2vHideLoading() {
        LogUtils.v("p2vHideLoading");
    }

    @Override
    public void p2vShowNormalView() {
        rootBinding.getRoot().setVisibility(View.VISIBLE);
        rootBinding.csvStatus.hideAll();
    }

    @Override
    public void p2vShowEmptyView() {
        rootBinding.getRoot().setVisibility(View.GONE);
        rootBinding.csvStatus.showEmpty();
    }

    @Override
    public void p2vShowLoadingView() {
        rootBinding.getRoot().setVisibility(View.GONE);
        rootBinding.csvStatus.showLoading();
    }

    @Override
    public void p2vShowRetryView() {
        rootBinding.getRoot().setVisibility(View.GONE);
        rootBinding.csvStatus.showTryAgain(v -> p2vClickStatusView());
    }

    @Override
    public void p2vClickStatusView() {

    }

    @Override
    public ContentStatusView p2vGetStatusView() {
        return rootBinding.csvStatus;
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragments = FragmentUtils.getFragments(getSupportFragmentManager());
        for (Fragment fragment : fragments) {
            if (fragment instanceof BaseFragment) {
                BaseFragment baseFragment = (BaseFragment) fragment;
                if (baseFragment.onFragmentBackPressed()) {
                    return;
                }
            }
        }
        super.onBackPressed();
    }
}
