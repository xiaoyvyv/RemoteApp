package com.xiaoyv.ui.dialog;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.chad.library.adapter.base.BaseBinderAdapter;
import com.xiaoyv.ui.databinding.UiDialogOptionsBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.jessyan.autosize.utils.AutoSizeUtils;

/**
 * OptionsDialog
 *
 * @author why
 * @since 2020/12/01
 **/
public class OptionsDialog extends AlertDialog {
    private final UiDialogOptionsBinding binding;
    private OptionsDialogItemBinder.OnItemChildClickListener clickListener;
    private BaseBinderAdapter multiTypeAdapter;
    private OptionsDialogItemBinder defaultBinder;
    private boolean canCloseable = true;


    public static OptionsDialog get(Context context) {
        return new OptionsDialog(context);
    }

    public OptionsDialog(@NonNull Context context) {
        this(context, 0);
    }

    protected OptionsDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.binding = UiDialogOptionsBinding.inflate(LayoutInflater.from(context));
        setView(this.binding.getRoot());
        init();
    }

    protected OptionsDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.binding = UiDialogOptionsBinding.inflate(LayoutInflater.from(context));
        setView(this.binding.getRoot());
        init();
    }

    private void init() {
        defaultBinder = new OptionsDialogItemBinder();
        multiTypeAdapter = new BaseBinderAdapter();
        multiTypeAdapter.addItemBinder(String.class, defaultBinder);
        binding.uiOptions.setAdapter(multiTypeAdapter);

        defaultBinder.setOnItemChildClickListener((view, dataBean, position, isLongClick) -> {
            ThreadUtils.runOnUiThreadDelayed(() -> {
                if (clickListener != null) {
                    clickListener.onItemChildClick(position);
                }
                if (canCloseable) {
                    dismiss();
                }
            }, 80);
        });
    }

    public OptionsDialog setTextColor(int textColor) {
        this.defaultBinder.setTextColor(textColor);
        return this;
    }

    public OptionsDialog setLastTextColor(int lastTextColor) {
        this.defaultBinder.setLastTextColor(lastTextColor);
        return this;
    }

    public OptionsDialog setTextSize(int textSize) {
        this.defaultBinder.setTextSize(textSize);
        return this;
    }

    public OptionsDialog setTextStyle(Typeface textStyle) {
        this.defaultBinder.setTextStyle(textStyle);
        return this;
    }

    public OptionsDialog setOnItemChildClickListener(OptionsDialogItemBinder.OnItemChildClickListener clickListener) {
        this.clickListener = clickListener;
        return this;
    }

    public OptionsDialog setOptions(List<String> options) {
        this.multiTypeAdapter.setList(options);
        this.multiTypeAdapter.notifyDataSetChanged();
        return this;
    }

    public OptionsDialog setOptions(CharSequence... charSequences) {
        List<CharSequence> list = Arrays.asList(charSequences);
        List<String> options = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            options.add(String.valueOf(list.get(i)));
        }
        this.multiTypeAdapter.setList(options);
        this.multiTypeAdapter.notifyDataSetChanged();
        return this;
    }

    public OptionsDialog setOptions(String... options) {
        this.multiTypeAdapter.setList(Arrays.asList(options));
        this.multiTypeAdapter.notifyDataSetChanged();
        return this;
    }

    @Override
    public void show() {
        super.show();
        Window window = this.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.width = ScreenUtils.getAppScreenWidth() - AutoSizeUtils.dp2px(Utils.getApp(), 100);
            attributes.dimAmount = 0.2f;
            window.setAttributes(attributes);
        }
    }

    @Override
    public void setCancelable(boolean flag) {
        this.canCloseable = flag;
        super.setCancelable(flag);
    }
}
