package com.xiaoyv.busines.base;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.LogUtils;
import com.drakeet.multitype.ItemViewBinder;

/**
 * BaseItemBinder
 *
 * @author why
 * @since 2020/12/01
 **/
public abstract class BaseItemBinder<T, VH extends RecyclerView.ViewHolder> extends ItemViewBinder<T, VH> {
    protected OnItemChildClickListener<T> onItemChildClickListener = (view, dataBean, position) ->
            LogUtils.v("empty impl click listener for the item'child");

    public void setOnItemChildClickListener(OnItemChildClickListener<T> onItemChildClickListener) {
        this.onItemChildClickListener = onItemChildClickListener;
    }

    public void addClickListener(@NonNull RecyclerView.ViewHolder holder, @NonNull View view) {
        int position = getPosition(holder);
        @SuppressWarnings("unchecked") T o = (T) getAdapterItems().get(position);
        view.setOnClickListener(v -> onItemChildClickListener.onItemChildClick(view, o, position));
    }

    public interface OnItemChildClickListener<BEAN> {
        /**
         * 子条目点击事件
         *
         * @param view     点击的 view
         * @param dataBean 数据
         * @param position 位置
         */
        void onItemChildClick(View view, BEAN dataBean, int position);
    }
}
