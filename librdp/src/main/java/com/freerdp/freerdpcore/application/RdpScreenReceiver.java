package com.freerdp.freerdpcore.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RdpScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            RdpApp.startDisconnectTimer();
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            RdpApp.cancelDisconnectTimer();
        }
    }
}
