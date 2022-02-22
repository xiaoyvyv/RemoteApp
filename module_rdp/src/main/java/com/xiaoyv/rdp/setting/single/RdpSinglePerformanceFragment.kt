package com.xiaoyv.rdp.setting.single

import android.view.View
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.blueprint.base.BaseFragment
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

    private fun saveState() {
        rdpConfig?.performanceSettings?.let {
            it.wallpaper = binding.svBackground.isChecked
            it.desktopComposition = binding.svDesktopCom.isChecked
            it.fullWindowDrag = binding.svDragContent.isChecked
            it.gfx = binding.svGfx.isChecked
            it.h264 = binding.svH264.isChecked
            it.remoteFx = binding.svRfx.isChecked
            it.fontSmoothing = binding.svSmFont.isChecked
            it.menuAnimations = binding.svMenuAnimation.isChecked
            it.theme = binding.svThemeStyle.isChecked
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
        fun newInstance() = RdpSinglePerformanceFragment()
    }
}