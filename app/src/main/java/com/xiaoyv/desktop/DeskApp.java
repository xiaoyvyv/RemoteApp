package com.xiaoyv.desktop;

import android.app.Application;

import com.blankj.utilcode.util.Utils;
import com.freerdp.freerdpcore.application.RdpApp;
import com.xiaoyv.blueprint.BluePrint;
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
}
