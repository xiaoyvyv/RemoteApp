package com.xiaoyv.rdp.setting.single

import android.view.View
import androidx.core.view.isVisible
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.blueprint.base.BaseFragment
import com.xiaoyv.desktop.rdp.databinding.RdpSettingSingleScreenBinding

/**
 * RdpSingleScreenFragment
 *
 * @author why
 * @since 2021/07/11
 **/
class RdpSingleScreenFragment : BaseFragment() {
    private lateinit var binding: RdpSettingSingleScreenBinding

    private var rdpConfig: RdpConfig? = null

    override fun createContentView(): View {
        binding = RdpSettingSingleScreenBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        val settingActivity = (activity as? RdpSingleSettingActivity) ?: return
        rdpConfig = settingActivity.rdpConfig
    }

    override fun initData() {
        rdpConfig?.screenSettings?.let {
            binding.slvColor.showByValue(it.colors)
            binding.slvResolution.showByValue(it.getResolutionString())

            if (it.isCustom()) {
                binding.sivWidth.uiValue = it.width.toString()
                binding.sivHeight.uiValue = it.height.toString()
            } else {
                binding.sivWidth.uiValue = "1024"
                binding.sivHeight.uiValue = "768"
            }
            showCustom(it.isCustom())
        }
    }

    override fun initListener() {
        binding.slvColor.onSelectIntListener = { value: Int, _: Int ->
            rdpConfig?.screenSettings?.colors = value
        }
        binding.slvResolution.onSelectStringListener = { value: String, _: Int ->
            val width = binding.sivWidth.uiValue.orEmpty().toIntOrNull() ?: 0
            val height = binding.sivHeight.uiValue.orEmpty().toIntOrNull() ?: 0

            rdpConfig?.screenSettings?.setResolution(value, width, height)
            showCustom(rdpConfig?.screenSettings?.isCustom() ?: false)
        }

        binding.sivWidth.onInputDoneListener = {
            val width = binding.sivWidth.uiValue.orEmpty().toIntOrNull() ?: 0
            val height = binding.sivHeight.uiValue.orEmpty().toIntOrNull() ?: 0
            rdpConfig?.screenSettings?.setResolution("custom", width, height)
        }
        binding.sivHeight.onInputDoneListener = {
            val width = binding.sivWidth.uiValue.orEmpty().toIntOrNull() ?: 0
            val height = binding.sivHeight.uiValue.orEmpty().toIntOrNull() ?: 0
            rdpConfig?.screenSettings?.setResolution("custom", width, height)
        }
    }

    private fun showCustom(boolean: Boolean) {
        binding.sivHeight.isVisible = boolean
        binding.sivWidth.isVisible = boolean
    }

    companion object {

        @JvmStatic
        fun newInstance() = RdpSingleScreenFragment()
    }
}