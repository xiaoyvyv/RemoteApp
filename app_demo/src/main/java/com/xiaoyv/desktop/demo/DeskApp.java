package com.xiaoyv.desktop.demo;

import android.app.Application;

import com.xiaoyv.blueprint.BluePrint;

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
        BluePrint.init(this);
//
//        Utils.init(this);
//        RdpApp.init(this);
//        BaseApp.init(this);
    }
}
