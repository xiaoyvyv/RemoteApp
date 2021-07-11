package com.xiaoyv.rdp.setting.single

import android.view.View
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.busines.base.BaseFragment
import com.xiaoyv.rdp.databinding.RdpSettingSinglePerformanceBinding

/**
 * RdpSinglePerformanceFragment
 *
 * @author why
 * @since 2021/07/11
 **/
class RdpSinglePerformanceFragment : BaseFragment() {
    private lateinit var binding: RdpSettingSinglePerformanceBinding

    private var rdpConfig: RdpConfig? = null

    override fun createContentView(): View {
        binding = RdpSettingSinglePerformanceBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initView() {
        val settingActivity = (activity as? RdpSingleSettingActivity) ?: return
        rdpConfig = settingActivity.rdpConfig
    }

    override fun initData() {
        rdpConfig?.performanceSettings?.let {
            binding.svBackground.isChecked = it.wallpaper
            binding.svDesktopCom.isChecked = it.desktopComposition
            binding.svDragContent.isChecked = it.fullWindowDrag
            binding.svGfx.isChecked = it.gfx
            binding.svH264.isChecked = it.h264
            binding.svRfx.isChecked = it.remoteFx
            binding.svSmFont.isChecked = it.fontSmoothing
            binding.svMenuAnimation.isChecked = it.menuAnimations
            binding.svThemeStyle.isChecked = it.theme
        }

    }

    companion object {

        @JvmStatic
        fun newInstance() = RdpSinglePerformanceFragment()
    }
}