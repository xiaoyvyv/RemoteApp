package com.xiaoyv.rdp.screen.listener;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.util.LogUtils;
import com.freerdp.freerdpcore.application.GlobalApp;
import com.freerdp.freerdpcore.application.SessionState;

public class RdpBroadcastReceiver extends BroadcastReceiver {

    private  SessionState session;

    public RdpBroadcastReceiver(SessionState session) {
        this.session = session;
    }

    public void setSession(SessionState session) {
        this.session = session;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // 仍然有一个有效的会话？
        if (session == null) {
            return;
        }

        // is this event for the current session?
        if (session.getInstance() != intent.getExtras().getLong(GlobalApp.EVENT_PARAM, -1))
            return;

        switch (intent.getExtras().getInt(GlobalApp.EVENT_TYPE, -1)) {
            case GlobalApp.FREERDP_EVENT_CONNECTION_SUCCESS:
                OnConnectionSuccess(context);
                break;

            case GlobalApp.FREERDP_EVENT_CONNECTION_FAILURE:
                OnConnectionFailure(context);
                break;
            case GlobalApp.FREERDP_EVENT_DISCONNECTED:
                OnDisconnected(context);
                break;
        }
    }

    private void OnConnectionSuccess(Context context) {
        LogUtils.e("OnConnectionSuccess");
    }

    private void OnConnectionFailure(Context context) {
        LogUtils.e("OnConnectionFailure");
    }

    private void OnDisconnected(Context context) {
        LogUtils.e("OnDisconnected");
    }
}