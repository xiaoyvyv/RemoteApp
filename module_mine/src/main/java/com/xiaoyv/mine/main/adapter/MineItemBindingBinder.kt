package com.xiaoyv.mine.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import com.xiaoyv.ui.base.BaseItemBindingBinder
import com.xiaoyv.mine.databinding.MineFragmentDialogItemBinding
import com.xiaoyv.mine.databinding.MineFragmentDialogItemColBinding
import com.xiaoyv.mine.main.contract.MineContract
import java.util.*

/**
 * MineItemBinder
 *
 * @author why
 * @since 2020/12/10
 */
class MineItemBindingBinder : BaseItemBindingBinder<List<Any>, MineFragmentDialogItemBinding>(),
    MineContract.Adapter {

    override fun onItemChange(fromPos: Int, toPos: Int) {
        Collections.swap(data, fromPos, toPos)
        adapter.notifyItemMoved(fromPos, toPos)
    }

    override fun onItemDelete(pos: Int) {
        data.removeAt(pos)
        adapter.notifyItemRemoved(pos)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): MineFragmentDialogItemBinding {
        return MineFragmentDialogItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun convert(
        holder: BinderVBHolder<MineFragmentDialogItemBinding>,
        binding: MineFragmentDialogItemBinding,
        data: List<Any>
    ) {
        binding.llContent.removeAllViews()
        data.forEach {
            val itemBinding = MineFragmentDialogItemColBinding.inflate(
                LayoutInflater.from(context),
                binding.llContent, true
            )
            itemBinding.tvTitle.text = it.toString()
        }
    }
}