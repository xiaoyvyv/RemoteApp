package com.xiaoyv.ui.setting;

import android.content.Context;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xiaoyv.ui.databinding.UiSettingInputBinding;
import com.xiaoyv.ui.databinding.UiSettingViewBinding;
import com.xiaoyv.ui.listener.SimpleTextChangeListener;

/**
 * AppSettingView
 *
 * @author why
 * @since 2020/11/29
 **/
@SuppressWarnings("UnusedReturnValue")
public class AppSettingView extends FrameLayout {
    private final UiSettingViewBinding binding;
    private final UiSettingInputBinding inputBinding;
    private final Context context;
    private AlertDialog inputDialog;

    public AppSettingView(@NonNull Context context) {
        this(context, null, 0);
    }

    public AppSettingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AppSettingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.binding = UiSettingViewBinding.inflate(LayoutInflater.from(context));
        this.inputBinding = UiSettingInputBinding.inflate(LayoutInflater.from(context));
        this.addView(binding.getRoot());
        init();
    }

    private void init() {
        binding.tvDesc.setVisibility(GONE);
        binding.smSwitch.setVisibility(GONE);

        // 输入框
        inputDialog = new AlertDialog.Builder(context)
                .setView(inputBinding.getRoot())
                .create();
        inputDialog.setCanceledOnTouchOutside(false);
        setInputLine(1);
        inputBinding.tvDone.setOnClickListener(v -> {
            setMessage(String.valueOf(inputBinding.etValue.getText()));
            KeyboardUtils.hideSoftInput(inputBinding.etValue);
            inputDialog.dismiss();
        });
        inputBinding.tvClear.setOnClickListener(v -> {
            KeyboardUtils.hideSoftInput(inputBinding.etValue);
            inputDialog.dismiss();
        });
        binding.clRoot.setOnClickListener(v -> {
            inputBinding.tvTitle.setText(getTitle());
            inputDialog.show();
            Window window = inputDialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawableResource(android.R.color.transparent);
                WindowManager.LayoutParams attributes = window.getAttributes();
                attributes.width = ScreenUtils.getAppScreenWidth() - 120;
                window.setAttributes(attributes);
            }
            inputBinding.etValue.requestFocus();
            KeyboardUtils.showSoftInput(inputBinding.etValue);
        });
    }

    public AppSettingView showSwitch() {
        binding.smSwitch.setVisibility(VISIBLE);
        binding.clRoot.setClickable(false);
        return this;
    }

    public AppSettingView hideSwitch() {
        binding.smSwitch.setVisibility(GONE);
        binding.clRoot.setClickable(true);
        return this;
    }


    public AppSettingView setTitle(@Nullable String title) {
        binding.tvTitle.setVisibility(StringUtils.isEmpty(title) ? GONE : VISIBLE);
        binding.tvTitle.setText(title);
        return this;
    }

    public AppSettingView setHint(@Nullable String hint) {
        inputBinding.etValue.setHint(hint);
        return this;
    }

    public AppSettingView setMessage(@Nullable String message) {
        binding.tvDesc.setVisibility(StringUtils.isEmpty(message) ? GONE : VISIBLE);
        binding.tvDesc.setText(message);
        return this;
    }

    public AppSettingView hideDivider() {
        binding.vDivider.setVisibility(INVISIBLE);
        return this;
    }

    public AppSettingView setInputTypeNumber(int maxLength) {
        inputBinding.etValue.setKeyListener(new DigitsKeyListener(false, true));
        inputBinding.etValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        return this;
    }

    public AppSettingView setInputLine(int line) {
        inputBinding.etValue.setMaxLines(line);
        inputBinding.etValue.setMinLines(line);
        inputBinding.etValue.setSingleLine(line == 1);
        inputBinding.etValue.setEllipsize(TextUtils.TruncateAt.END);
        return this;
    }

    public AppSettingView setSwitchChangeListener(@Nullable CompoundButton.OnCheckedChangeListener listener) {
        showSwitch();
        binding.smSwitch.setOnCheckedChangeListener(listener);
        return this;
    }

    public AppSettingView addInputChangeListener(@Nullable SimpleTextChangeListener listener) {
        hideSwitch();
        inputBinding.etValue.addTextChangedListener(listener);
        return this;
    }

    public String getTitle() {
        return String.valueOf(binding.tvTitle.getText());
    }

    public String getMessage() {
        return String.valueOf(binding.tvDesc.getText());
    }

    public int getValue() {
        String value = String.valueOf(binding.tvDesc.getText());
        int i = 0;
        try {
            i = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return i;
    }

    public boolean isSwitch() {
        return binding.smSwitch.isChecked();
    }
}
