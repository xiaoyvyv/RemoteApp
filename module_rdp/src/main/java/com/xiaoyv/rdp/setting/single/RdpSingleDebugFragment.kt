package com.xiaoyv.rdp.setting.single

import android.view.View
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.blueprint.base.BaseFragment
import com.xiaoyv.desktop.rdp.databinding.RdpSettingSingleDebugBinding

/**
 * RdpSingleDebugFragment
 *
 * @author why
 * @since 2021/07/11
 **/
class RdpSingleDebugFragment : BaseFragment() {
    private lateinit var binding: RdpSettingSingleDebugBinding

    private var rdpConfig: RdpConfig? = null

    override fun createContentView(): View {
        binding = RdpSettingSingleDebugBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        val settingActivity = (activity as? RdpSingleSettingActivity) ?: return
        rdpConfig = settingActivity.rdpConfig
    }

    override fun initData() {
        rdpConfig?.debugSettings?.let {
            binding.svAsyncChannel.isChecked = it.asyncChannel
            binding.svAsyncUpdate.isChecked = it.asyncUpdate
            binding.svAsyncInput.isChecked = it.asyncInput
            binding.svDebugLevel.showByValue(it.debugLevel)
        }
    }

    override fun initListener() {
        binding.svDebugLevel.onSelectStringListener = { _: String, index: Int ->
            val level = RdpConfig.DebugSettings.levels[index]
            rdpConfig?.debugSettings?.debugLevel = level
        }
    }

    private fun saveState() {
        rdpConfig?.debugSettings?.let {
            it.asyncChannel = binding.svAsyncChannel.isChecked
            it.asyncUpdate = binding.svAsyncUpdate.isChecked
            it.asyncInput = binding.svAsyncInput.isChecked
        }
    }

    /**
     * 是否消费返回事件，仅 Fragment 重写该方法
     *
     * @return Fragment 是否消费返回事件
     */
    override fun onFragmentBackPressed(): Boolean {
        saveState()
        return super.onFragmentBackPressed()
    }

    companion object {
        @JvmStatic
        fun newInstance() = RdpSingleDebugFragment()
    }
}