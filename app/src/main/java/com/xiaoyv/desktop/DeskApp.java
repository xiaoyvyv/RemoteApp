package com.xiaoyv.desktop;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.xiaoyv.busines.BaseApp;
import com.xiaoyv.librdp.RdpEngineApp;

/**
 * DeskApp
 *
 * @author why
 * @since 2020/11/28
 **/
public class DeskApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        BaseApp.init(this);
        RdpEngineApp.init(this);
    }
}
