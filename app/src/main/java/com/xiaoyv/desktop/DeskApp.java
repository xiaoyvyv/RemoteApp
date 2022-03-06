package com.xiaoyv.desktop;

import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.os.MessageQueue;

import androidx.multidex.MultiDex;

import com.freerdp.freerdpcore.application.RdpApp;
import com.xiaoyv.business.BaseApp;

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

        Looper.myLooper().getQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {

                return false;
            }
        });
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
