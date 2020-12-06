package com.xiaoyv.ssh.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.TimeUtils;
import com.xiaoyv.busines.base.BaseItemBinder;
import com.xiaoyv.busines.room.entity.SshEntity;
import com.xiaoyv.ssh.databinding.SshFragmentMainItemBinding;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * SshListAdapter
 *
 * @author why
 * @since 2020/11/29
 **/
public class SshListBinder extends BaseItemBinder<SshEntity, SshListBinder.ViewHolder> {

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull LayoutInflater layoutInflater, @NotNull ViewGroup viewGroup) {
        SshFragmentMainItemBinding binding = SshFragmentMainItemBinding.inflate(layoutInflater, viewGroup, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, SshEntity sshEntity) {
        viewHolder.binding.tvLabel.setText(sshEntity.label);
        viewHolder.binding.tvAccount.setText(String.format(Locale.getDefault(), "%s：%s", sshEntity.account, sshEntity.ip));
        viewHolder.binding.tvTime.setText(TimeUtils.getFriendlyTimeSpanByNow(sshEntity.lastTime));
        addClickListener(viewHolder, viewHolder.binding.getRoot());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public SshFragmentMainItemBinding binding;

        public ViewHolder(@NonNull View itemView, SshFragmentMainItemBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }
}
