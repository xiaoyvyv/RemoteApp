package com.xiaoyv.busines.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xiaoyv.busines.room.entity.RdpEntity;
import com.xiaoyv.business.R;
import com.xiaoyv.business.databinding.BusinessFragmentRootBinding;
import com.xiaoyv.ui.status.ContentStatusView;

/**
 * BaseFragment
 *
 * @author why
 * @since 2020/11/28
 **/
public abstract class BaseFragment extends Fragment implements IBaseView {
    private BusinessFragmentRootBinding rootBinding;
    protected View rootView;
    protected Activity activity;
    protected boolean isLoaded = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = getActivity();
        Bundle arguments = getArguments();
        if (arguments != null) {
            initArgumentsData(arguments);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootBinding = BusinessFragmentRootBinding.inflate(inflater, container, false);
            rootBinding.flRoot.addView(createContentView());
            rootView = rootBinding.getRoot();
        }
        initView();
        return rootView;
    }

    protected abstract View createContentView();

    protected void initArgumentsData(Bundle arguments) {

    }

    protected abstract void initView();

    protected abstract void initData();

    protected void initListener() {
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isLoaded) {
            // androidx 版本的 Fragment 弃用了 setUserVisibleHint()
            // 所以懒加载通过 在可见时会调用 onResume() 实现
            initData();
            initListener();
            isLoaded = false;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 重置，需重新加载数据
        isLoaded = true;
    }

    @Override
    public void p2vShowToast(String msg) {
        ToastUtils.getDefaultMaker()
                .setBgColor(ColorUtils.getColor(R.color.ui_system_c1))
                .setGravity(Gravity.CENTER, 0, 0)
                .setTextColor(ColorUtils.getColor(R.color.ui_text_c5))
                .setTextSize(15)
                .show(msg);
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
        rootBinding.csvStatus.hideAll();
    }

    @Override
    public void p2vShowEmptyView() {
        rootBinding.csvStatus.showEmpty();
    }

    @Override
    public void p2vShowLoadingView() {
        rootBinding.csvStatus.showLoading();
    }

    @Override
    public void p2vShowRetryView() {
        rootBinding.csvStatus.showTryAgain(v -> p2vClickStatusView());
    }


    @Override
    public void p2vClickStatusView() {

    }

    @Override
    public ContentStatusView p2vGetStatusView() {
        return rootBinding.csvStatus;
    }

}
