package com.xiaoyv.busines.base;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.LogUtils;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.xiaoyv.ui.listener.SimpleFastClickListener;

/**
 * BaseItemBinder
 *
 * @author why
 * @since 2020/12/01
 **/
public abstract class BaseItemBinder<T> extends com.chad.library.adapter.base.binder.BaseItemBinder<T, BaseViewHolder> {
    protected RecyclerView rvRootRecycler;

    protected OnItemChildClickListener<T> onItemChildClickListener = (view, dataBean, position, longClick) ->
            LogUtils.v("empty impl click listener for the item's child");

    public void setOnItemChildClickListener(OnItemChildClickListener<T> onItemChildClickListener) {
        this.onItemChildClickListener = onItemChildClickListener;
    }

    public void addClickListener(@NonNull RecyclerView.ViewHolder holder, @NonNull View view) {
        int position = holder.getBindingAdapterPosition();

         T o = (T) getAdapterItems().get(position);
         view.setOnFastLimitClickListener{

        }
        view.setOnClickListener(new SimpleFastClickListener(1000) {
            @Override
            public void onMultiClick(View v) {
                onItemChildClickListener.onItemChildClick(view, o, position, false);
            }
        });
        view.setOnLongClickListener(v -> {
            onItemChildClickListener.onItemChildClick(view, o, position, true);
            return true;
        });
    }

    public void bindRecycler(RecyclerView rvItem) {
        this.rvRootRecycler = rvItem;
    }

    public interface OnItemChildClickListener<BEAN> {
        /**
         * 子条目点击事件
         *
         * @param view        点击的 view
         * @param dataBean    数据
         * @param position    位置
         * @param isLongClick 是否长按
         */
        void onItemChildClick(View view, BEAN dataBean, int position, boolean isLongClick);
    }
}
