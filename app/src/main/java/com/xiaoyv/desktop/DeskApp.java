package com.xiaoyv.desktop;

import android.app.Application;
import android.content.Context;

import androidx.multidex.MultiDex;

import com.freerdp.freerdpcore.application.RdpApp;
import com.xiaoyv.busines.BaseApp;

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
        BaseApp.init(this);
        RdpApp.init(this);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
