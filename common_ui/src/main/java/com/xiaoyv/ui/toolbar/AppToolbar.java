package com.xiaoyv.ui.toolbar;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.blankj.utilcode.util.BarUtils;
import com.xiaoyv.ui.databinding.UiViewToolbarBinding;

/**
 * WhyToolbar
 *
 * @author why
 * @since 2020/11/29
 **/
@SuppressWarnings("UnusedReturnValue")
public class AppToolbar extends FrameLayout {
    protected final UiViewToolbarBinding binding;
    protected final Context context;

    public AppToolbar(@NonNull Context context) {
        this(context, null, 0);
    }

    public AppToolbar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppToolbar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.binding = UiViewToolbarBinding.inflate(LayoutInflater.from(context));
        this.addView(binding.getRoot());
        init();
    }

    private void init() {
        this.binding.ivEnd.setVisibility(INVISIBLE);
        this.binding.ivStart.setVisibility(INVISIBLE);
        this.binding.ivStart.setOnClickListener(v -> {
            if (context instanceof Activity) {
                ((Activity) context).finish();
            }
        });
        setNeedStatusBar(true);
    }

    /**
     * 状态栏占位
     *
     * @param needStatus 是否需要占位
     */
    public AppToolbar setNeedStatusBar(boolean needStatus) {
        ConstraintLayout.LayoutParams layoutParams = new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, BarUtils.getStatusBarHeight());
        binding.vStatus.setLayoutParams(layoutParams);
        binding.vStatus.setVisibility(needStatus ? VISIBLE : GONE);
        return this;
    }

    public AppToolbar setTitle(@Nullable String title) {
        binding.tvTitle.setText(title);
        return this;
    }

    public AppToolbar setStartIcon(@DrawableRes int icon) {
        binding.ivStart.setImageResource(icon);
        binding.ivStart.setVisibility(VISIBLE);
        return this;
    }

    public AppToolbar setEndIcon(@DrawableRes int icon) {
        binding.ivEnd.setImageResource(icon);
        binding.ivEnd.setVisibility(VISIBLE);
        return this;
    }

    public AppToolbar hideEndIcon() {
        binding.ivEnd.setVisibility(INVISIBLE);
        return this;
    }

    public AppToolbar hideStartIcon() {
        binding.ivStart.setVisibility(INVISIBLE);
        return this;
    }

    public AppToolbar hideDivider() {
        binding.vDivider.setVisibility(INVISIBLE);
        return this;
    }

    public AppToolbar setStartClickListener(@Nullable View.OnClickListener clickListener) {
        binding.ivStart.setVisibility(VISIBLE);
        binding.ivStart.setOnClickListener(clickListener);
        return this;
    }

    public AppToolbar setEndClickListener(@Nullable View.OnClickListener clickListener) {
        binding.ivEnd.setVisibility(VISIBLE);
        binding.ivEnd.setOnClickListener(clickListener);
        return this;
    }

}
