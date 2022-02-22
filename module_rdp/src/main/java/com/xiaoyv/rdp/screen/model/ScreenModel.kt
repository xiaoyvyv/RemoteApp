package com.xiaoyv.rdp.screen.model

import android.content.res.Configuration
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ScreenUtils
import com.blankj.utilcode.util.Utils
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.rdp.screen.contract.ScreenContract
import kotlin.math.max

/**
 * ScreenModel
 *
 * @author why
 * @since 2020/12/02
 */
class ScreenModel : ScreenContract.Model {

    override fun p2mApplyConfig(rdpConfig: RdpConfig, landscape: Boolean) {
        BarUtils.isSupportNavBar()
        val screenWidth = ScreenUtils.getScreenWidth()
        val screenHeight = ScreenUtils.getScreenHeight()

        val screenSettings = rdpConfig.screenSettings
        when {
            screenSettings.isAutomatic() -> {
                when {
                    // 大屏幕设备
                    Utils.getApp().resources.configuration.screenLayout and
                            Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE -> {
                        screenSettings.width = screenWidth
                        screenSettings.height = screenHeight
                    }
                    // 小屏幕设备
                    else -> {
                        // 横屏直接用屏幕宽高
                        if (landscape) {
                            screenSettings.width = screenWidth
                            screenSettings.height = screenHeight
                        }
                        // 竖屏用长边作为高，然后 长:宽 = 16:10
                        else {
                            val screenMax: Int = max(screenWidth, screenHeight)
                            screenSettings.width = (screenMax.toFloat() * 1.6f).toInt()
                            screenSettings.height = screenMax
                        }
                    }
                }
            }
            screenSettings.isFitScreen() -> {
                screenSettings.width = screenWidth
                screenSettings.height = screenHeight
            }
        }

        // 异形屏幕适配，如大圆角屏幕边缘点击不到

        LogUtils.i(
            "屏幕分辨率信息",
            "宽度：$screenWidth", "高度：$screenHeight",
            "RDP 宽度：${screenSettings.width}", "RDP 高度：${screenSettings.height}"
        )
    }
}