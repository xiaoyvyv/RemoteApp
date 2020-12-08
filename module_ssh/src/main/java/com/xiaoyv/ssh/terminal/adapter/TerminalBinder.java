package com.xiaoyv.ssh.terminal.adapter;

import android.annotation.SuppressLint;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ColorUtils;
import com.romide.terminal.emulatorview.EmulatorView;
import com.xiaoyv.busines.base.BaseItemBinder;
import com.xiaoyv.busines.bean.ssh.KeyCodeBean;
import com.xiaoyv.ssh.R;
import com.xiaoyv.ssh.databinding.SshActivityTerminalKeyBinding;

import org.jetbrains.annotations.NotNull;


/**
 * TerminalBinder
 *
 * @author why
 * @since 2020/12/08
 **/
public class TerminalBinder extends BaseItemBinder<KeyCodeBean, TerminalBinder.ViewHolder> {
    private EmulatorView evTerminal;

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull LayoutInflater layoutInflater, @NotNull ViewGroup viewGroup) {
        SshActivityTerminalKeyBinding binding = SshActivityTerminalKeyBinding.inflate(layoutInflater, viewGroup, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, KeyCodeBean keyCodeBean) {
        viewHolder.binding.tvKey.setText(keyCodeBean.getValue());
        viewHolder.binding.tvKey.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    viewHolder.binding.tvKey.setBackgroundColor(ColorUtils.getColor(R.color.ui_system_divider));
                    evTerminal.onKeyDown(keyCodeBean.getKey(), new KeyEvent(event.getDownTime(), event.getEventTime(), event.getAction(), keyCodeBean.getKey(), 0, event.getMetaState()));
                    return true;
                case MotionEvent.ACTION_MOVE:
                    return true;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    viewHolder.binding.tvKey.setBackgroundColor(ColorUtils.getColor(R.color.ui_system_background));
                    evTerminal.onKeyUp(keyCodeBean.getKey(), new KeyEvent(event.getDownTime(), event.getEventTime(), event.getAction(), keyCodeBean.getKey(), 0, event.getMetaState()));
                    return true;
            }
            return false;
        });
    }

    public void bindTerminal(EmulatorView evTerminal) {
        this.evTerminal = evTerminal;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final SshActivityTerminalKeyBinding binding;

        public ViewHolder(@NonNull View itemView, SshActivityTerminalKeyBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }
}
