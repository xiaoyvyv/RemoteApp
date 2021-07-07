package com.xiaoyv.busines;

import android.app.Activity;
import android.app.Application;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.ProcessUtils;
import com.tencent.bugly.crashreport.CrashReport;
import com.xiaoyv.busines.exception.RxExceptionHandler;
import com.xiaoyv.busines.exception.RxGlobalExceptionHandler;

import java.util.Locale;

import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import me.jessyan.autosize.AutoSize;
import me.jessyan.autosize.AutoSizeConfig;
import me.jessyan.autosize.onAdaptListener;
import me.jessyan.autosize.utils.AutoSizeLog;
import me.jessyan.autosize.utils.ScreenUtils;

/**
 * BaseApp
 *
 * @author why
 * @since 2020/11/28
 **/
public class BaseApp {
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

    private static void autoSizeInit(Application application) {
        AutoSize.checkAndInit(application);
        AutoSizeConfig.getInstance()
                .setDesignWidthInDp(360)
                .setDesignHeightInDp(640)
                // 屏幕适配监听器
                .setOnAdaptListener(new onAdaptListener() {
                    @Override
                    public void onAdaptBefore(Object target, Activity activity) {
                        // 使用以下代码, 可以解决横竖屏切换时的屏幕适配问题
                        // 使用以下代码, 可支持 Android 的分屏或缩放模式, 但前提是在分屏或缩放模式下当用户改变您 App 的窗口大小时
                        // 系统会重绘当前的页面, 经测试在某些机型, 某些情况下系统不会主动重绘当前页面, 所以这时您需要自行重绘当前页面
                        // ScreenUtils.getScreenSize(activity) 的参数一定要不要传 Application!!!
                        AutoSizeConfig.getInstance().setScreenWidth(ScreenUtils.getScreenSize(activity)[0]);
                        AutoSizeConfig.getInstance().setScreenHeight(ScreenUtils.getScreenSize(activity)[1]);
                        AutoSize.autoConvertDensityOfGlobal(activity);
                    }

                    @Override
                    public void onAdaptAfter(Object target, Activity activity) {

                    }
                });

    }
}
