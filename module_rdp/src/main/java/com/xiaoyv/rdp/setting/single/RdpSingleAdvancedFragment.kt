package com.xiaoyv.rdp.setting.single

import android.view.View
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.busines.base.BaseFragment
import com.xiaoyv.rdp.databinding.RdpSettingSingleAdvanceBinding

/**
 * RdpSingleAdvancedFragment
 *
 * @author why
 * @since 2021/07/11
 **/
class RdpSingleAdvancedFragment : BaseFragment() {
    private lateinit var binding: RdpSettingSingleAdvanceBinding

    private var rdpConfig: RdpConfig? = null

    override fun createContentView(): View {
        binding = RdpSettingSingleAdvanceBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        val settingActivity = (activity as? RdpSingleSettingActivity) ?: return
        rdpConfig = settingActivity.rdpConfig
    }

    override fun initData() {
        binding.svGateway.isChecked = rdpConfig?.enableGatewaySettings ?: false

        rdpConfig?.advancedSettings?.let {
            binding.svRedirectSdcard.isChecked = it.redirectSDCard
            binding.svRedirectAudio.showByValue(it.redirectSound)
            binding.svRedirectMicrophone.isChecked = it.redirectMicrophone
            binding.svSecurity.showByValue(it.security)
            binding.svConsole.isChecked = it.consoleMode
            binding.sivWorkDir.uiValue = it.workDir
            binding.sivProgramPath.uiValue = it.remoteProgram
        }

    }

    override fun initListener() {
        binding.svRedirectAudio.onSelectStringListener = { _: String, index: Int ->
            rdpConfig?.advancedSettings?.redirectSound = index
        }
        binding.svSecurity.onSelectStringListener = { _: String, index: Int ->
            rdpConfig?.advancedSettings?.security = index
        }
    }

    private fun saveState() {
        rdpConfig?.enableGatewaySettings = binding.svGateway.isChecked

        rdpConfig?.advancedSettings?.let {
            it.redirectSDCard = binding.svRedirectSdcard.isChecked
            it.redirectMicrophone = binding.svRedirectMicrophone.isChecked
            it.consoleMode = binding.svConsole.isChecked
            it.workDir = binding.sivWorkDir.uiValue.orEmpty()
            it.remoteProgram = binding.sivProgramPath.uiValue.orEmpty()
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
        fun newInstance() = RdpSingleAdvancedFragment()
    }
}