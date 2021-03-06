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
import com.drakeet.multitype.MultiTypeAdapter;
import com.xiaoyv.ui.databinding.UiDialogOptionsBinding;

import java.util.Arrays;
import java.util.List;

/**
 * OptionsDialog
 *
 * @author why
 * @since 2020/12/01
 **/
public class OptionsDialog extends AlertDialog {
    private final UiDialogOptionsBinding binding;
    private OptionsDialogItemBinder.OnItemChildClickListener clickListener;
    private MultiTypeAdapter multiTypeAdapter;
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
        multiTypeAdapter = new MultiTypeAdapter();
        multiTypeAdapter.register(String.class, defaultBinder);
        binding.uiOptions.setAdapter(multiTypeAdapter);
        defaultBinder.setOnItemChildClickListener(position -> {
            if (canCloseable) {
                dismiss();
            }
            if (clickListener != null) {
                clickListener.onItemChildClick(position);
            }
        });
    }

    public void setTextColor(int textColor) {
        this.defaultBinder.setTextColor(textColor);
    }

    public void setLastTextColor(int lastTextColor) {
        this.defaultBinder.setLastTextColor(lastTextColor);
    }

    public void setTextSize(int textSize) {
        this.defaultBinder.setTextSize(textSize);
    }

    public void setTextStyle(Typeface textStyle) {
        this.defaultBinder.setTextStyle(textStyle);
    }

    public void setOnItemChildClickListener(OptionsDialogItemBinder.OnItemChildClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setOptions(List<String> options) {
        this.multiTypeAdapter.setItems(options);
        this.multiTypeAdapter.notifyDataSetChanged();
    }

    public void setOptions(String... options) {
        this.multiTypeAdapter.setItems(Arrays.asList(options));
        this.multiTypeAdapter.notifyDataSetChanged();
    }

    @Override
    public void show() {
        super.show();
        Window window = this.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(android.R.color.transparent);
            WindowManager.LayoutParams attributes = window.getAttributes();
            attributes.width = ScreenUtils.getAppScreenWidth() - 120;
            window.setAttributes(attributes);
        }
    }

    @Override
    public void setCancelable(boolean flag) {
        this.canCloseable = flag;
        super.setCancelable(flag);
    }
}
