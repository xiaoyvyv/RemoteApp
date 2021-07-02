package com.xiaoyv.mine.main.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoyv.busines.base.BaseItemBinder;
import com.xiaoyv.mine.R;
import com.xiaoyv.mine.databinding.MineFragmentDialogItemBinding;
import com.xiaoyv.mine.databinding.MineFragmentDialogItemColBinding;
import com.xiaoyv.mine.main.contract.MineContract;
import com.xiaoyv.ui.scroll.HorScrollView;

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

    private final Context context;

    public MineItemBinder(Context context) {
        this.context = context;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull LayoutInflater layoutInflater, @NotNull ViewGroup viewGroup) {
        MineFragmentDialogItemBinding binding = MineFragmentDialogItemBinding.inflate(layoutInflater, viewGroup, false);
        return new ViewHolder(binding.getRoot(), binding);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, List col) {
        viewHolder.binding.llContent.removeAllViews();
        for (Object o : col) {
            MineFragmentDialogItemColBinding binding = MineFragmentDialogItemColBinding.inflate(LayoutInflater.from(context), viewHolder.binding.llContent, false);
            viewHolder.binding.llContent.addView(binding.getRoot());
            binding.tvTitle.setText(String.valueOf(o));
        }
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
