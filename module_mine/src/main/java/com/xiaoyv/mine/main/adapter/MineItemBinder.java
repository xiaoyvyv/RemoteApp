package com.xiaoyv.mine.main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.GsonUtils;
import com.xiaoyv.busines.base.BaseItemBinder;
import com.xiaoyv.mine.databinding.MineFragmentDialogItemBinding;
import com.xiaoyv.mine.main.contract.MineContract;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * MineItemBinder
 *
 * @author why
 * @since 2020/12/10
 **/
@SuppressWarnings("rawtypes")
public class MineItemBinder extends BaseItemBinder<List, MineItemBinder.ViewHolder> implements MineContract.Adapter {

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull LayoutInflater layoutInflater, @NotNull ViewGroup viewGroup) {
        MineFragmentDialogItemBinding binding = MineFragmentDialogItemBinding.inflate(layoutInflater, viewGroup, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, List col) {
        viewHolder.binding.tvTitle.setText(GsonUtils.toJson(col));
    }

    @Override
    public void onItemChange(int fromPos, int toPos) {
        Collections.swap(getAdapterItems(), fromPos, toPos);
        getAdapter().notifyItemMoved(fromPos, toPos);
    }

    @Override
    public void onItemDelete(int pos) {
        getAdapterItems().remove(pos);
        getAdapter().notifyItemRemoved(pos);
    }

    /**
     * ViewHolder
     *
     * @author why
     * @since 2020/12/10
     **/
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final MineFragmentDialogItemBinding binding;

        public ViewHolder(@NonNull View itemView, MineFragmentDialogItemBinding binding) {
            super(itemView);
            this.binding = binding;
        }
    }
}
