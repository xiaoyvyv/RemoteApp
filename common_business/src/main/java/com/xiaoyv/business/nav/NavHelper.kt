package com.xiaoyv.business.nav

import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ActivityUtils
import com.xiaoyv.business.config.NavigationKey
import com.xiaoyv.business.config.NavigationPath

/**
 * NavHelper
 *
 * @author why
 * @since 2022/3/4
 */
object NavHelper {

    @JvmStatic
    fun jumpFileEditorActivity(filePath: String) {
        ARouter.getInstance().build(NavigationPath.PATH_ACTIVITY_EDITOR)
            .withString(NavigationKey.KEY_STRING, filePath)
            .navigation()
    }
}