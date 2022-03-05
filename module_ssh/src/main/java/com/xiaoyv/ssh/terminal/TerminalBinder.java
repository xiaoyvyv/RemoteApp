package com.xiaoyv.ssh.terminal;

import android.annotation.SuppressLint;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ColorUtils;
import com.romide.terminal.emulatorview.EmulatorView;
import com.xiaoyv.business.bean.ssh.KeyCodeBean;
import com.xiaoyv.desktop.ssh.R;
import com.xiaoyv.desktop.ssh.databinding.SshActivityTerminalKeyBinding;
import com.xiaoyv.widget.binder.BaseItemBindingBinder;


/**
 * TerminalBinder
 *
 * @author why
 * @since 2020/12/08
 **/
public class TerminalBinder extends BaseItemBindingBinder<KeyCodeBean, SshActivityTerminalKeyBinding> {
    private EmulatorView evTerminal;

    public void bindTerminal(EmulatorView evTerminal) {
        this.evTerminal = evTerminal;
    }

    @NonNull
    @Override
    public SshActivityTerminalKeyBinding onCreateViewBinding(@NonNull LayoutInflater layoutInflater, @NonNull ViewGroup viewGroup, int i) {
        return SshActivityTerminalKeyBinding.inflate(layoutInflater, viewGroup, false);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void convert(@NonNull BinderVBHolder<SshActivityTerminalKeyBinding> holder, @NonNull SshActivityTerminalKeyBinding binding, KeyCodeBean data) {
        binding.tvKey.setText(data.getValue());
        binding.tvKey.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    binding.tvKey.setBackgroundColor(ColorUtils.getColor(R.color.ui_system_divider));
                    evTerminal.onKeyDown(data.getKey(), new KeyEvent(event.getDownTime(), event.getEventTime(), event.getAction(), data.getKey(), 0, event.getMetaState()));
                    return true;
                case MotionEvent.ACTION_MOVE:
                    return true;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    binding.tvKey.setBackgroundColor(ColorUtils.getColor(R.color.ui_system_background));
                    evTerminal.onKeyUp(data.getKey(), new KeyEvent(event.getDownTime(), event.getEventTime(), event.getAction(), data.getKey(), 0, event.getMetaState()));
                    return true;
            }
            return false;
        });
    }
}
