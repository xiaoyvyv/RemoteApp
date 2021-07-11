package com.xiaoyv.rdp.setting.single

import android.os.Bundle
import android.view.View
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.busines.base.BaseFragment
import com.xiaoyv.busines.config.NavigationKey
import com.xiaoyv.rdp.databinding.RdpSettingSingleDebugBinding
import com.xiaoyv.rdp.databinding.RdpSettingSinglePerformanceBinding

/**
 * RdpSingleDebugFragment
 *
 * @author why
 * @since 2021/07/11
 **/
class RdpSingleDebugFragment: BaseFragment() {
    private lateinit var binding: RdpSettingSingleDebugBinding

    override fun createContentView(): View {
        binding = RdpSettingSingleDebugBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
    }

    override fun initData() {
    }

    companion object {
        @JvmStatic
        fun newInstance() = RdpSingleDebugFragment()
    }
}