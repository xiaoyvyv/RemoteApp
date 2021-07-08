package com.xiaoyv.busines;

import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.ProcessUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiaoyv.busines.exception.RxExceptionHandler;
import com.xiaoyv.busines.exception.RxGlobalExceptionHandler;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;
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
        // 非主进程不初始化
        if (!ProcessUtils.isMainProcess()) {
            return;
        }
        // 腾讯Bugly
        CrashReport.initCrashReport(application, "c22595ab77", false);
        // 路由
        ARouter.init(application);

        // 设置全局异常处理器
        RxExceptionHandler.setExceptionHandler(new RxGlobalExceptionHandler());
        // 设置全局未捕获异常拦截器
        RxJavaPlugins.setErrorHandler(Throwable::printStackTrace);
        // 屏幕适配
        autoSizeInit(application);
    }

    /**
     * 屏幕分辨率适配
     *
     * @param application application
     */
    private static void autoSizeInit(Application application) {
        AutoSize.checkAndInit(application);
        AutoSizeConfig.getInstance()
                .setDesignWidthInDp((int) MAX_WIDTH_DP);
    }
}
