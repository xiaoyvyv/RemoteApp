package com.xiaoyv.mine.main.adapter

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.xiaoyv.mine.main.contract.MineContract

/**
 * ItemTouchHelperCallback
 *
 * @author why
 * @since 2020/12/10
 */
class MineItemHelper(private val mAdapter: MineContract.Adapter) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {
        // 上下拖动
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        // 从右向左
        val swipeFlag = ItemTouchHelper.LEFT
        return makeMovementFlags(dragFlag, swipeFlag)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
    ): Boolean {
        // 返回true 表示目标 viewHolder 已经移到目标位置
        mAdapter.onItemChange(viewHolder.bindingAdapterPosition, target.bindingAdapterPosition)
        return true
    }

    /**
     * 返回 true 支持长按拖动，false 不支持
     *
     * @return 支持长按拖动
     */
    override fun isLongPressDragEnabled(): Boolean {
        return false
    }

    /**
     * 返回 true 支持滑动，false 不支持
     *
     * @return 支持滑动
     */
    override fun isItemViewSwipeEnabled(): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        mAdapter.onItemDelete(viewHolder.bindingAdapterPosition)
    }
}