package com.xiaoyv.ssh.main

import android.view.LayoutInflater
import android.view.ViewGroup
import com.blankj.utilcode.util.TimeUtils
import com.xiaoyv.busines.room.entity.SshEntity
import com.xiaoyv.desktop.ssh.databinding.SshFragmentMainItemBinding
import com.xiaoyv.widget.binder.BaseItemBindingBinder
import java.util.*

/**
 * SshListAdapter
 *
 * @author why
 * @since 2020/11/29
 */
class SshListBindingBinder : BaseItemBindingBinder<SshEntity, SshFragmentMainItemBinding>() {
    override fun onCreateViewBinding(
        layoutInflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): SshFragmentMainItemBinding {
        return SshFragmentMainItemBinding.inflate(layoutInflater, parent, false)
    }

    override fun convert(
        holder: BinderVBHolder<SshFragmentMainItemBinding>,
        binding: SshFragmentMainItemBinding,
        data: SshEntity
    ) {
        binding.tvLabel.text = data.label
        binding.tvAccount.text = String.format(Locale.getDefault(), "%s：%s", data.account, data.ip)
        binding.tvTime.text = TimeUtils.getFriendlyTimeSpanByNow(data.lastTime)
        holder.addClickListener(binding.root, data)
    }
}