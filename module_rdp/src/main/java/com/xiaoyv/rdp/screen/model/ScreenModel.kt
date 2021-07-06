package com.xiaoyv.rdp.screen.model

import android.content.res.Configuration
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

    override fun p2mApplyConfig(rdpConfig: RdpConfig) {
        val screenSettings = rdpConfig.screenSettings
        LogUtils.e("屏幕分辨率设置：", screenSettings.getResolutionString())

        if (screenSettings.isAutomatic()) {
            if (Utils.getApp().resources.configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK >= Configuration.SCREENLAYOUT_SIZE_LARGE
            ) {
                // 大屏幕设备，即平板电脑：只需使用屏幕信息
                screenSettings.height = ScreenUtils.getAppScreenHeight()
                screenSettings.width = ScreenUtils.getAppScreenWidth()
            } else {
                // 小屏幕设备，即电话：自动使用屏幕的最大边长，并将其设置为 16:10 分辨率
                val screenMax: Int =
                    max(ScreenUtils.getAppScreenWidth(), ScreenUtils.getAppScreenHeight())
                screenSettings.height = screenMax
                screenSettings.width = (screenMax.toFloat() * 1.6f).toInt()
            }
        }
        // 适配屏幕大小
        if (screenSettings.isFitScreen()) {
            screenSettings.height = ScreenUtils.getAppScreenHeight()
            screenSettings.width = ScreenUtils.getAppScreenWidth()
        }
    }
}