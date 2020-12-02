package com.xiaoyv.rdp.screen.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.Utils;
import com.freerdp.freerdpcore.presentation.SessionActivity;
import com.xiaoyv.busines.base.BaseMvpActivity;
import com.xiaoyv.busines.room.entity.RdpEntity;
import com.xiaoyv.rdp.databinding.RdpActivityScreenBinding;
import com.xiaoyv.rdp.screen.contract.ScreenContract;
import com.xiaoyv.rdp.screen.presenter.ScreenPresenter;

/**
 * ScreenView
 *
 * @author why
 * @since 2020/12/02
 **/
public class ScreenActivity extends BaseMvpActivity<ScreenContract.View, ScreenPresenter> implements ScreenContract.View {

    private RdpActivityScreenBinding binding;

    public static void openSelf(RdpEntity rdpEntity) {
        // 获取连接参考
        Intent sessionIntent = new Intent(Utils.getApp(), SessionActivity.class);
        Bundle rdpExtra = new Bundle();
        sessionIntent.putExtras(rdpExtra);
        ActivityUtils.startActivity(sessionIntent);
    }

    @Override
    protected ScreenPresenter createPresenter() {
        return new ScreenPresenter();
    }

    @Override
    protected View createContentView() {
        binding = RdpActivityScreenBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
    }

    @Override
    protected void initData() {

    }
}
