package com.xiaoyv.ui.dialog;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.LogUtils;
import com.drakeet.multitype.ItemViewBinder;
import com.xiaoyv.ui.R;
import com.xiaoyv.ui.databinding.UiDialogOptionsItemBinding;

import org.jetbrains.annotations.NotNull;

/**
 * OptionsDialog
 *
 * @author why
 * @since 2020/12/01
 **/
public class OptionsDialogItemBinder extends ItemViewBinder<String, OptionsDialogItemBinder.ViewHolder> {
    private OnItemChildClickListener clickListener;
    @ColorInt
    private int textColor;
    @ColorInt
    private int lastTextColor;
    private int textSize;
    private Typeface textStyle;

    public OptionsDialogItemBinder() {
        textSize = 16;
        textStyle = Typeface.DEFAULT;
        textColor = ColorUtils.getColor(R.color.ui_text_c1);
        lastTextColor = ColorUtils.getColor(R.color.ui_text_c1);
    }

    public void setOnItemChildClickListener(OnItemChildClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public void setLastTextColor(int lastTextColor) {
        this.lastTextColor = lastTextColor;
    }

    public void setTextSize(int textSize) {
        this.textSize = textSize;
    }

    public void setTextStyle(Typeface textStyle) {
        this.textStyle = textStyle;
    }


    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NotNull LayoutInflater layoutInflater, @NotNull ViewGroup viewGroup) {
        UiDialogOptionsItemBinding itemBinding = UiDialogOptionsItemBinding.inflate(layoutInflater, viewGroup, false);
        return new ViewHolder(itemBinding.getRoot(), itemBinding);
    }

    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, String option) {
        viewHolder.itemBinding.tvOptions.setText(option);
        viewHolder.itemBinding.tvOptions.setTypeface(textStyle);
        viewHolder.itemBinding.tvOptions.setTextSize(textSize);
        viewHolder.itemBinding.tvOptions.setOnClickListener(v -> {
            if (this.clickListener != null) {
                this.clickListener.onItemChildClick(getPosition(viewHolder));
            }
        });

        if (getPosition(viewHolder) == getAdapterItems().size() - 1) {
            viewHolder.itemBinding.uiView.setVisibility(View.GONE);
            viewHolder.itemBinding.tvOptions.setTextColor(lastTextColor);
        } else {
            viewHolder.itemBinding.uiView.setVisibility(View.VISIBLE);
            viewHolder.itemBinding.tvOptions.setTextColor(textColor);
        }
    }

    /**
     * ViewHolder
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final UiDialogOptionsItemBinding itemBinding;

        public ViewHolder(@NonNull View itemView, UiDialogOptionsItemBinding itemBinding) {
            super(itemView);
            this.itemBinding = itemBinding;
        }
    }

    public interface OnItemChildClickListener {
        /**
         * 选项点击事件
         *
         * @param position 事件
         */
        void onItemChildClick(int position);
    }
}
