package com.xiaoyv.ui.setting;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.method.DigitsKeyListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.KeyboardUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xiaoyv.ui.R;
import com.xiaoyv.ui.databinding.UiSettingInputBinding;
import com.xiaoyv.ui.databinding.UiSettingViewBinding;
import com.xiaoyv.ui.dialog.OptionsDialog;
import com.xiaoyv.ui.listener.SimpleTextChangeListener;

/**
 * AppSettingView
 *
 * @author why
 * @since 2020/11/29
 **/
public class UiSettingView extends FrameLayout {
    protected String sharePreferenceName = "TEMP";
    protected final UiSettingViewBinding binding;
    protected final UiSettingInputBinding inputBinding;
    protected final Context context;
    protected AlertDialog inputDialog;
    private String requiredText;

    /**
     * XML 可配置的值
     */
    protected String spKey;
    protected String uiTitle;
    protected String uiMessage;
    protected String uiValueString;
    protected CharSequence[] uiListTitle;
    protected CharSequence[] uiListValue;
    protected boolean uiValueBoolean;
    protected float uiValueFloat;
    protected int uiValueInteger;
    protected int uiValueList;
    @UiSettingViewType
    protected int uiType = UiSettingViewType.TYPE_STRING;

    protected CompoundButton.OnCheckedChangeListener checkedChangeListener;
    private OptionsDialog optionsDialog;

    public UiSettingView(@NonNull Context context, @NonNull String spKey) {
        this(context, null, 0);
        this.spKey = spKey;
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
    }

    /**
     * 初始化参数
     *
     * @param attrs attrs
     */
    private void initAttr(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.UiSettingView);
        spKey = array.getString(R.styleable.UiSettingView_ui_key);
        uiTitle = array.getString(R.styleable.UiSettingView_ui_title);
        uiMessage = array.getString(R.styleable.UiSettingView_ui_message);
        uiValueString = array.getString(R.styleable.UiSettingView_ui_default_string);
        uiValueBoolean = array.getBoolean(R.styleable.UiSettingView_ui_default_boolean, false);
        uiValueFloat = array.getFloat(R.styleable.UiSettingView_ui_default_float, 0);
        uiValueInteger = array.getInteger(R.styleable.UiSettingView_ui_default_integer, 0);
        uiListTitle = array.getTextArray(R.styleable.UiSettingView_ui_list_title);
        uiListValue = array.getTextArray(R.styleable.UiSettingView_ui_list_value);
        uiValueList = array.getInteger(R.styleable.UiSettingView_ui_default_list, 0);
        uiType = array.getInteger(R.styleable.UiSettingView_ui_type, UiSettingViewType.TYPE_STRING);
        array.recycle();

        // 设置标题和提示
        setTitle(uiTitle);
        setMessage(uiMessage);

        if (StringUtils.isEmpty(spKey)) {
            spKey = "default_key";
            // 若设置了 TYPE 为非字符串类型，但没有设置 SP_KEY，则抛异常
            if (uiType != UiSettingViewType.TYPE_STRING) {
                throw new RuntimeException("非字符串类型，请先设置：SP_KEY");
            }
        }
    }

    /**
     * 初始化
     */
    private void init() {
        getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // 检测父布局是否为 UiSettingScreen
            if (getParent() != null && getParent() instanceof UiSettingScreen) {
                UiSettingScreen screen = (UiSettingScreen) getParent();
                String preferenceName = screen.getSharePreferenceName();
                if (!StringUtils.isEmpty(preferenceName)) {
                    sharePreferenceName = preferenceName;
                }
            }
            setDefaultValue();
        });

        // 输入框
        inputDialog = new AlertDialog.Builder(context).setView(inputBinding.getRoot()).create();
        inputDialog.setCanceledOnTouchOutside(false);
        setInputLine(1);
        inputBinding.tvClear.setOnClickListener(v -> {
            KeyboardUtils.hideSoftInput(inputBinding.etValue);
            inputDialog.dismiss();
        });

        // 点击事件
        binding.clRoot.setOnClickListener(v -> {
            // 布尔类型不显示弹框
            if (uiType == UiSettingViewType.TYPE_BOOLEAN) {
                binding.smSwitch.setChecked(!binding.smSwitch.isChecked());
                return;
            }
            if (uiType == UiSettingViewType.TYPE_LIST_STRING) {
                if (optionsDialog != null) {
                    optionsDialog.show();
                }
                return;
            }
            if (uiType == UiSettingViewType.TYPE_LIST_INTEGER) {
                if (optionsDialog != null) {
                    optionsDialog.show();
                }
                return;
            }

            inputBinding.tvTitle.setText(getTitle());
            String message = getMessage();
            inputBinding.etValue.setText(message);
            if (!StringUtils.isEmpty(message)) {
                inputBinding.etValue.setSelection(0, message.length());
            }
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
        initType();
        refreshUi();
    }

    private void setDefaultValue() {
        // 第一次则保存默认属性
        SPUtils utils = SPUtils.getInstance(sharePreferenceName);
        if (!utils.contains(spKey)) {
            if (uiType == UiSettingViewType.TYPE_BOOLEAN) {
                utils.put(spKey, uiValueBoolean);
            } else if (uiType == UiSettingViewType.TYPE_FLOAT) {
                utils.put(spKey, uiValueFloat);
            } else if (uiType == UiSettingViewType.TYPE_INTEGER) {
                utils.put(spKey, uiValueInteger);
            } else if (uiType == UiSettingViewType.TYPE_STRING) {
                utils.put(spKey, uiValueString);
            } else if (uiType == UiSettingViewType.TYPE_LIST_STRING) {
                if (!ArrayUtils.isEmpty(uiListValue) && uiValueList < uiListValue.length) {
                    utils.put(spKey, String.valueOf(uiListValue[uiValueList]));
                }
            } else if (uiType == UiSettingViewType.TYPE_LIST_INTEGER) {
                if (!ArrayUtils.isEmpty(uiListValue) && uiValueList < uiListValue.length) {
                    try {
                        utils.put(spKey, Integer.parseInt(String.valueOf(uiListValue[uiValueList])));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void initType() {
        hideSwitch();
        binding.tvDesc.setVisibility(VISIBLE);
        switch (uiType) {
            case UiSettingViewType.TYPE_BOOLEAN: {
                showSwitch();
                boolean b = SPUtils.getInstance(sharePreferenceName).getBoolean(spKey, uiValueBoolean);
                binding.smSwitch.setChecked(b);
                // 开关事件
                binding.smSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    SPUtils.getInstance(sharePreferenceName).put(spKey, isChecked);
                    if (checkedChangeListener != null) {
                        checkedChangeListener.onCheckedChanged(buttonView, isChecked);
                    }
                    refreshUi();
                });
                break;
            }
            case UiSettingViewType.TYPE_FLOAT: {
                inputBinding.etValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                inputBinding.etValue.setFilters(new DigitsKeyListener[]{DigitsKeyListener.getInstance(".0123456789")});
                inputBinding.tvDone.setOnClickListener(v -> {
                    KeyboardUtils.hideSoftInput(inputBinding.etValue);
                    inputDialog.dismiss();
                    try {
                        String input = String.valueOf(inputBinding.etValue.getText());
                        SPUtils.getInstance(sharePreferenceName).put(spKey, Float.parseFloat(input));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    refreshUi();
                });
                break;
            }
            case UiSettingViewType.TYPE_INTEGER: {
                inputBinding.etValue.setInputType(InputType.TYPE_CLASS_NUMBER);
                inputBinding.etValue.setFilters(new DigitsKeyListener[]{DigitsKeyListener.getInstance("0123456789")});
                inputBinding.tvDone.setOnClickListener(v -> {
                    KeyboardUtils.hideSoftInput(inputBinding.etValue);
                    inputDialog.dismiss();
                    try {
                        String input = String.valueOf(inputBinding.etValue.getText());
                        SPUtils.getInstance(sharePreferenceName).put(spKey, Integer.parseInt(input));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                    refreshUi();
                });
                break;
            }
            case UiSettingViewType.TYPE_STRING: {
                inputBinding.etValue.setInputType(InputType.TYPE_CLASS_TEXT);
                inputBinding.tvDone.setOnClickListener(v -> {
                    KeyboardUtils.hideSoftInput(inputBinding.etValue);
                    inputDialog.dismiss();
                    String input = String.valueOf(inputBinding.etValue.getText());
                    SPUtils.getInstance(sharePreferenceName).put(spKey, input);
                    refreshUi();
                });
                break;
            }
            case UiSettingViewType.TYPE_LIST_INTEGER: {
                optionsDialog = new OptionsDialog(context)
                        .setOptions(uiListTitle)
                        .setOnItemChildClickListener(position -> {
                            optionsDialog.dismiss();
                            try {
                                String input = String.valueOf(uiListValue[position]);
                                SPUtils.getInstance(sharePreferenceName).put(spKey, Integer.parseInt(input));
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            refreshUi();
                        });
                break;
            }
            case UiSettingViewType.TYPE_LIST_STRING: {
                optionsDialog = new OptionsDialog(context)
                        .setOptions(uiListTitle)
                        .setOnItemChildClickListener(position -> {
                            optionsDialog.dismiss();
                            try {
                                String input = String.valueOf(uiListValue[position]);
                                SPUtils.getInstance(sharePreferenceName).put(spKey, input);
                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }
                            refreshUi();
                        });
                break;
            }
            default:

                break;
        }
    }

    public void refreshUi() {
        SPUtils utils = SPUtils.getInstance(sharePreferenceName);
        if (uiType == UiSettingViewType.TYPE_BOOLEAN) {
            setMessage(utils.getBoolean(spKey, uiValueBoolean));
        } else if (uiType == UiSettingViewType.TYPE_FLOAT) {
            setMessage(utils.getFloat(spKey, uiValueFloat));
        } else if (uiType == UiSettingViewType.TYPE_INTEGER) {
            setMessage(utils.getInt(spKey, uiValueInteger));
        } else if (uiType == UiSettingViewType.TYPE_STRING) {
            setMessage(utils.getString(spKey, uiValueString));
        } else if (uiType == UiSettingViewType.TYPE_LIST_STRING) {
            String defaultValue = "";
            if (!ArrayUtils.isEmpty(uiListValue) && uiValueList < uiListValue.length) {
                defaultValue = String.valueOf(uiListValue[uiValueList]);
            }
            setMessage(utils.getString(spKey, defaultValue));
        } else if (uiType == UiSettingViewType.TYPE_LIST_INTEGER) {
            if (!ArrayUtils.isEmpty(uiListValue) && uiValueList < uiListValue.length) {
                try {
                    setMessage(utils.getInt(spKey, Integer.parseInt(String.valueOf(uiListValue[uiValueList]))));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            int defaultValue = 0;
            if (!ArrayUtils.isEmpty(uiListValue) && uiValueList < uiListValue.length) {
                try {
                    defaultValue = Integer.parseInt(String.valueOf(uiListValue[uiValueList]));
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
            setMessage(utils.getInt(spKey, defaultValue));
        }
    }

    public UiSettingView showSwitch() {
        binding.smSwitch.setVisibility(VISIBLE);
        return this;
    }

    public UiSettingView hideSwitch() {
        binding.smSwitch.setVisibility(GONE);
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


    public UiSettingView setMessage(int message) {
        return setMessage(String.valueOf(message));
    }

    public UiSettingView setMessage(float message) {
        return setMessage(String.valueOf(message));
    }

    public UiSettingView setMessage(@Nullable String message) {
        if (StringUtils.isEmpty(message) && !StringUtils.isEmpty(requiredText)) {
            binding.tvDesc.setVisibility(VISIBLE);
            binding.tvDesc.setText(requiredText);
            binding.tvDesc.setTextColor(ColorUtils.getColor(R.color.ui_system_error));
            return this;
        }
        binding.tvDesc.setVisibility(StringUtils.isEmpty(message) ? GONE : VISIBLE);
        binding.tvDesc.setText(message);
        binding.tvDesc.setTextColor(ColorUtils.getColor(R.color.ui_text_c3));
        return this;
    }

    public UiSettingView setMessage(boolean message) {
        binding.tvDesc.setVisibility(VISIBLE);
        binding.tvDesc.setText(message ? StringUtils.getString(R.string.ui_view_setting_open) : StringUtils.getString(R.string.ui_view_setting_close));
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

    public UiSettingView setBold(boolean bold) {
        binding.tvTitle.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        return this;
    }

    public UiSettingView setRequiredText(String tips) {
        this.requiredText = StringUtils.isEmpty(tips) ? StringUtils.getString(R.string.ui_view_setting_empty) : tips;
        if (StringUtils.isEmpty(binding.tvDesc.getText())) {
            binding.tvDesc.setVisibility(VISIBLE);
            binding.tvDesc.setText(this.requiredText);
            binding.tvDesc.setTextColor(ColorUtils.getColor(R.color.ui_system_error));
        }
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

    public UiSettingView setOnItemClickListener(@Nullable View.OnClickListener listener) {
        hideSwitch();
        inputBinding.clRoot.setOnClickListener(listener);
        return this;
    }

    public String getTitle() {
        return String.valueOf(binding.tvTitle.getText());
    }

    public String getMessage() {
        String message = String.valueOf(binding.tvDesc.getText());
        if (StringUtils.equals(requiredText, message) || StringUtils.equals(StringUtils.getString(R.string.ui_view_setting_empty), message)) {
            return null;
        }
        return message;
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

    public String getSpKey() {
        return spKey;
    }
}
