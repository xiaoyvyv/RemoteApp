package com.xiaoyv.busines;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.ProcessUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiaoyv.blueprint.BluePrint;
import com.xiaoyv.blueprint.BluePrintApp;

import me.jessyan.autosize.AutoSize;
import me.jessyan.autosize.AutoSizeConfig;

/**
 * BaseApp
 *
 * @author why
 * @since 2020/11/28
 **/
public class BaseApp {
    public static final float MAX_WIDTH_DP = 375;

    public static void init(Application application) {
        // 框架初始化
        BluePrint.init(application);

        // 非主进程不初始化
        if (!ProcessUtils.isMainProcess()) {
            return;
        }
        // 腾讯Bugly
        CrashReport.initCrashReport(application, "c22595ab77", false);
        // 路由
        ARouter.init(application);
    }
}
