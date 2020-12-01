package com.xiaoyv.rdp.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.drakeet.multitype.ItemViewBinder;
import com.xiaoyv.busines.base.BaseItemBinder;
import com.xiaoyv.busines.room.entity.RdpEntity;
import com.xiaoyv.rdp.databinding.RdpFragmentMainItemBinding;

import org.jetbrains.annotations.NotNull;

/**
 * RdpListAdapter
 *
 * @author why
 * @since 2020/11/29
 **/
public class RdpListBinder extends BaseItemBinder<RdpEntity, RdpListBinder.ViewHolder> {

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull LayoutInflater layoutInflater, @NotNull ViewGroup viewGroup) {
        RdpFragmentMainItemBinding binding = RdpFragmentMainItemBinding.inflate(layoutInflater, viewGroup, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, RdpEntity rdpEntity) {
        viewHolder.binding.tvLabel.setText(rdpEntity.label);
        viewHolder.binding.tvAccount.setText(rdpEntity.account);
        viewHolder.binding.tvTime.setText(TimeUtils.getFriendlyTimeSpanByNow(rdpEntity.lastTime));
        addClickListener(viewHolder, viewHolder.binding.getRoot());
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public RdpFragmentMainItemBinding binding;

        public ViewHolder(@NonNull View itemView, RdpFragmentMainItemBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }
}
