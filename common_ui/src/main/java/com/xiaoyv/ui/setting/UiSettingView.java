package com.xiaoyv.ui.setting;

import android.content.Context;
import android.content.res.TypedArray;
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
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xiaoyv.ui.R;
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
public class UiSettingView extends FrameLayout {
    public static String GLOBAL_SP_NAME = "TEMP";
    protected final UiSettingViewBinding binding;
    protected final UiSettingInputBinding inputBinding;
    protected final Context context;
    protected AlertDialog inputDialog;

    /**
     * XML 可配置的值
     */
    protected String spKey;
    protected String uiTitle;
    protected String uiMessage;
    protected String uiValueString;
    protected boolean uiValueBoolean;
    protected float uiValueFloat;
    protected int uiValueInteger;
    @UiSettingViewType
    protected int uiType = UiSettingViewType.TYPE_STRING;

    protected CompoundButton.OnCheckedChangeListener checkedChangeListener;

    public UiSettingView(@NonNull Context context) {
        this(context, null, 0);
    }

    public UiSettingView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public UiSettingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.binding = UiSettingViewBinding.inflate(LayoutInflater.from(context));
        this.inputBinding = UiSettingInputBinding.inflate(LayoutInflater.from(context));
        this.addView(binding.getRoot());
        initAttr(attrs);
        init();
        initType();
    }

    /**
     * 初始化参数
     *
     * @param attrs attrs
     */
    private void initAttr(AttributeSet attrs) {
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.UiSettingView);
        spKey = array.getString(R.styleable.UiSettingView_ui_key);
        uiTitle = array.getString(R.styleable.UiSettingView_ui_title);
        uiMessage = array.getString(R.styleable.UiSettingView_ui_message);
        uiValueString = array.getString(R.styleable.UiSettingView_ui_default_string);
        uiValueBoolean = array.getBoolean(R.styleable.UiSettingView_ui_default_boolean, false);
        uiValueFloat = array.getFloat(R.styleable.UiSettingView_ui_default_float, 0);
        uiValueInteger = array.getInteger(R.styleable.UiSettingView_ui_default_integer, 0);
        uiType = array.getInteger(R.styleable.UiSettingView_ui_type, UiSettingViewType.TYPE_STRING);
        array.recycle();

        SPUtils utils = SPUtils.getInstance(GLOBAL_SP_NAME);
        if (uiType == UiSettingViewType.TYPE_BOOLEAN) {


        } else if (uiType == UiSettingViewType.TYPE_FLOAT) {
        } else if (uiType == UiSettingViewType.TYPE_INTEGER) {
            binding.tvDesc.setVisibility(VISIBLE);
        } else if (uiType == UiSettingViewType.TYPE_STRING) {
        }
    }

    /**
     * 初始化
     */
    private void init() {
        // 输入框
        inputDialog = new AlertDialog.Builder(context)
                .setView(inputBinding.getRoot())
                .create();
        inputDialog.setCanceledOnTouchOutside(false);
        setInputLine(1);
        inputBinding.tvDone.setOnClickListener(v -> {
            KeyboardUtils.hideSoftInput(inputBinding.etValue);
            inputDialog.dismiss();
            processInput(String.valueOf(inputBinding.etValue.getText()));
        });
        inputBinding.tvClear.setOnClickListener(v -> {
            KeyboardUtils.hideSoftInput(inputBinding.etValue);
            inputDialog.dismiss();
        });
        binding.clRoot.setOnClickListener(v -> {
            inputBinding.tvTitle.setText(getTitle());
            inputBinding.etValue.setText(getMessage());
            inputBinding.etValue.setSelection(0, getMessage().length());
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


    private void initType() {
        hideSwitch();
        switch (uiType) {
            case UiSettingViewType.TYPE_BOOLEAN: {
                showSwitch();
                binding.smSwitch.setChecked(SPUtils.getInstance(GLOBAL_SP_NAME).getBoolean(spKey, uiValueBoolean));
                // 开关事件
                binding.smSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    SPUtils.getInstance(GLOBAL_SP_NAME).put(spKey, isChecked);
                    if (checkedChangeListener != null) {
                        checkedChangeListener.onCheckedChanged(buttonView, isChecked);
                    }
                });
                break;
            }
            case UiSettingViewType.TYPE_FLOAT: {
                break;
            }
            case UiSettingViewType.TYPE_INTEGER: {
                binding.tvDesc.setVisibility(VISIBLE);

                break;
            }
            case UiSettingViewType.TYPE_STRING: {
                binding.tvDesc.setVisibility(VISIBLE);

                break;
            }
            default:

                break;
        }
    }

    /**
     * 解析输入，保存到 SP
     *
     * @param input 输入
     */
    private void processInput(String input) {
        switch (uiType) {
            case UiSettingViewType.TYPE_BOOLEAN:


                break;
            case UiSettingViewType.TYPE_FLOAT:

                break;
            case UiSettingViewType.TYPE_INTEGER:

                break;
            case UiSettingViewType.TYPE_STRING:

                break;
            default:

                break;
        }
    }

    public UiSettingView showSwitch() {
        binding.smSwitch.setVisibility(VISIBLE);
        binding.clRoot.setClickable(false);
        return this;
    }

    public UiSettingView hideSwitch() {
        binding.smSwitch.setVisibility(GONE);
        binding.clRoot.setClickable(true);
        return this;
    }


    public UiSettingView setTitle(@Nullable String title) {
        binding.tvTitle.setVisibility(StringUtils.isEmpty(title) ? GONE : VISIBLE);
        binding.tvTitle.setText(title);
        return this;
    }

    public UiSettingView setHint(@Nullable String hint) {
        inputBinding.etValue.setHint(hint);
        return this;
    }

    public UiSettingView setMessage(@Nullable String message) {
        binding.tvDesc.setVisibility(StringUtils.isEmpty(message) ? GONE : VISIBLE);
        binding.tvDesc.setText(message);
        return this;
    }

    public UiSettingView hideDivider() {
        binding.vDivider.setVisibility(INVISIBLE);
        return this;
    }

    public UiSettingView setInputNumberType(int maxLength) {
        inputBinding.etValue.setKeyListener(new DigitsKeyListener(false, true));
        inputBinding.etValue.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});
        return this;
    }

    public UiSettingView setInputLine(int line) {
        inputBinding.etValue.setMaxLines(line);
        inputBinding.etValue.setMinLines(line);
        inputBinding.etValue.setSingleLine(line == 1);
        inputBinding.etValue.setEllipsize(TextUtils.TruncateAt.END);
        return this;
    }

    public UiSettingView setSwitchChangeListener(@Nullable CompoundButton.OnCheckedChangeListener listener) {
        showSwitch();
        this.checkedChangeListener = listener;
        return this;
    }

    public UiSettingView addInputChangeListener(@Nullable SimpleTextChangeListener listener) {
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
