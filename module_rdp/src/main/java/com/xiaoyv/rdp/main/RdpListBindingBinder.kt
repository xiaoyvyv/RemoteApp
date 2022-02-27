package com.xiaoyv.rdp.main

import android.view.LayoutInflater
import android.view.ViewGroup
import com.blankj.utilcode.util.TimeUtils
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.desktop.rdp.databinding.RdpFragmentMainItemBinding
import com.xiaoyv.widget.binder.BaseItemBindingBinder

/**
 * RdpListAdapter
 *
 * @author why
 * @since 2020/11/29
 */
class RdpListBindingBinder : BaseItemBindingBinder<RdpEntity, RdpFragmentMainItemBinding>() {
    override fun convert(
        holder: BinderVBHolder<RdpFragmentMainItemBinding>,
        binding: RdpFragmentMainItemBinding,
        data: RdpEntity
    ) {
        binding.tvLabel.text = data.label
        binding.tvAccount.text = String.format("%s：%s", data.account, data.ip)
        binding.tvTime.text = TimeUtils.getFriendlyTimeSpanByNow(data.lastTime)

        holder.addClickListener(binding.root, data,2500)
    }

    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = RdpFragmentMainItemBinding.inflate(layoutInflater, parent, false)
}