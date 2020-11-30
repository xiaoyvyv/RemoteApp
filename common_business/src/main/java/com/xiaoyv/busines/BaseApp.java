package com.xiaoyv.busines;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.ProcessUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiaoyv.busines.exception.RxGlobalExceptionHandler;
import com.xiaoyv.busines.exception.RxExceptionHandler;

/**
 * BaseApp
 *
 * @author why
 * @since 2020/11/28
 **/
public class BaseApp  {
    public static void init(Application application) {
        // 非主进程不初始化
        if (!ProcessUtils.isMainProcess()) {
            return;
        }
        // 腾讯Bugly
        CrashReport.initCrashReport(application, "c22595ab77", false);
        // 路由
        ARouter.init(application);

        //设置全局异常处理器
        RxExceptionHandler.setExceptionHandler(new RxGlobalExceptionHandler());
    }
}
