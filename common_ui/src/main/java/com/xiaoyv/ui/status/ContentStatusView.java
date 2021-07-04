package com.xiaoyv.ui.status;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.Utils;
import com.xiaoyv.ui.R;
import com.xiaoyv.ui.databinding.UiViewContentBinding;

import me.jessyan.autosize.utils.AutoSizeUtils;

/**
 * ContentStatusView
 *
 * @author why
 * @since 2020/12/01
 **/
public class ContentStatusView extends FrameLayout {
    private UiViewContentBinding binding;

    public ContentStatusView(@NonNull Context context) {
        this(context, null, 0);
    }

    public ContentStatusView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ContentStatusView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.binding = UiViewContentBinding.inflate(LayoutInflater.from(context));
        this.addView(binding.getRoot());
        init();
    }

    private void init() {
        hideAll();
        getViewTreeObserver().addOnGlobalLayoutListener(this::fitTopWindows);
    }

    public void showEmpty() {
        showEmpty(StringUtils.getString(R.string.ui_view_status_empty));
    }

    public void showEmpty(String text) {
        hideAll();
        this.setVisibility(VISIBLE);
        binding.ivPic.setVisibility(VISIBLE);
        binding.tvStatus.setVisibility(VISIBLE);
        binding.tvStatus.setText(text);
    }

    public void showLoading() {
        hideAll();
        this.setVisibility(VISIBLE);
        binding.pbLoading.setVisibility(VISIBLE);
    }

    public void showTryAgain(View.OnClickListener clickListener) {
        showTryAgain(StringUtils.getString(R.string.ui_view_status_empty), clickListener);
    }

    public void showTryAgain(String text, View.OnClickListener clickListener) {
        showTryAgain(text, StringUtils.getString(R.string.ui_view_status_refresh), clickListener);
    }

    public void showTryAgain(String text, String btnText, View.OnClickListener clickListener) {
        hideAll();
        this.setVisibility(VISIBLE);
        binding.ivPic.setVisibility(VISIBLE);
        binding.tvStatus.setVisibility(VISIBLE);
        binding.btRefresh.setVisibility(VISIBLE);
        binding.tvStatus.setText(text);
        binding.btRefresh.setText(btnText);
        binding.btRefresh.setOnClickListener(clickListener);
    }

    public void hideAll() {
        this.setVisibility(GONE);
        binding.pbLoading.setVisibility(GONE);
        binding.tvStatus.setVisibility(GONE);
        binding.ivPic.setVisibility(GONE);
        binding.btRefresh.setVisibility(GONE);
    }


    /**
     * 状态栏占位、标题栏
     */
    public void fitTopWindows() {
        int height = BarUtils.getStatusBarHeight() + AutoSizeUtils.dp2px(Utils.getApp(),44);
        LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        binding.vFakeTop.setLayoutParams(layoutParams);
    }


    /**
     * 状态栏占位
     *
     * @param margin 是否需要占位
     */
    public void fitBottomWindows(int margin) {
        LinearLayoutCompat.LayoutParams layoutParams = new LinearLayoutCompat.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, margin);
        binding.vFakeBottom.setLayoutParams(layoutParams);
    }
}
