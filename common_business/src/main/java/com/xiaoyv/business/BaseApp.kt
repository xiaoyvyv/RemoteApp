package com.xiaoyv.business;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.ProcessUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiaoyv.blueprint.BluePrint;

/**
 * BaseApp
 *
 * @author why
 * @since 2020/11/28
 **/
public class BaseApp {
    public static void init(Application application) {
        // 框架初始化
        BluePrint.init(application, true);

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
