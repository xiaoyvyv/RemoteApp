package com.xiaoyv.mine.main.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.xiaoyv.mine.main.contract.MineContract;

/**
 * ItemTouchHelperCallback
 *
 * @author why
 * @since 2020/12/10
 **/
public class MineItemHelper extends ItemTouchHelper.Callback {

    private final MineContract.Adapter mAdapter;

    public MineItemHelper(MineContract.Adapter mAdapter) {
        this.mAdapter = mAdapter;
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        // 上下拖动
        int dragFlag = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
        // 从右向左
        int swipeFlag = ItemTouchHelper.LEFT;
        return makeMovementFlags(dragFlag, swipeFlag);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        // 返回true 表示目标 viewHolder 已经移到目标位置
        mAdapter.onItemChange(viewHolder.getAdapterPosition(), target.getAdapterPosition());
        return true;
    }

    /**
     * 返回 true 支持长按拖动，false 不支持
     *
     * @return 支持长按拖动
     */
    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }
    /**
     * 返回 true 支持滑动，false 不支持
     *
     * @return 支持滑动
     */
    @Override
    public boolean isItemViewSwipeEnabled() {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        mAdapter.onItemDelete(viewHolder.getAdapterPosition());
    }
}