package com.freerdp.freerdpcore.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.NetworkUtils;
import com.blankj.utilcode.util.Utils;
import com.freerdp.freerdpcore.domain.RdpConfig;
import com.freerdp.freerdpcore.domain.RdpSession;
import com.freerdp.freerdpcore.presentation.ApplicationSettingsActivity;
import com.freerdp.freerdpcore.services.EventListener;
import com.freerdp.freerdpcore.services.LibFreeRDP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class RdpApp implements EventListener {
    private static volatile RdpApp globalApp;

    /**
     * Rdp广播通知定义
     */
    public static final String EVENT_TYPE = "EVENT_TYPE";
    public static final String EVENT_PARAM = "EVENT_PARAM";
    public static final String ACTION_EVENT_FREERDP = "com.freerdp.freerdp.event.freerdp";

    /**
     * Rdp广播事件  开设连接
     */
    public static final int FREERDP_EVENT_PREPARE_CONNECT = 0;
    /**
     * Rdp广播事件  连接成功
     */
    public static final int FREERDP_EVENT_CONNECTION_SUCCESS = 1;
    /**
     * Rdp广播事件  连接失败
     */
    public static final int FREERDP_EVENT_CONNECTION_FAILURE = 2;
    /**
     * Rdp广播事件  连接断开中
     */
    public static final int FREERDP_EVENT_DISCONNECTING = 3;
    /**
     * Rdp广播事件  连接断开
     */
    public static final int FREERDP_EVENT_DISCONNECTED = 4;


    public static boolean ConnectedToMobileWork = false;

    public static Map<Long, RdpSession> sessionMap;

    // 屏幕关闭后用于断开会话的计时器
    public static Timer disconnectTimer = null;
    public static Application application;

    private RdpApp() {

    }

    public static void init(@NonNull Application application) {
        RdpApp.application = application;
        if (globalApp == null) {
            synchronized (RdpApp.class) {
                if (globalApp == null) {
                    globalApp = new RdpApp();
                }
            }
        }
        globalApp.onCreate(application);
    }

    private void onCreate(Application application) {
        // 初始化首选项
        ApplicationSettingsActivity.get(application);

        sessionMap = Collections.synchronizedMap(new HashMap<>(0));

        LibFreeRDP.setEventListener(this);


        ConnectedToMobileWork = NetworkUtils.is5G() || NetworkUtils.is4G();

        // 亮屏息屏广播监听
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        application.registerReceiver(new RdpScreenReceiver(), filter);
    }


    // 断开屏幕开/关事件的处理
    public static void startDisconnectTimer() {
        final int timeoutMinutes = ApplicationSettingsActivity.getDisconnectTimeout(application);
        if (timeoutMinutes > 0) {
            // 开始断开连接超时...
            disconnectTimer = new Timer();
            disconnectTimer.schedule(new DisconnectTask(), timeoutMinutes * 60 * 1000);
        }
    }

    static public void cancelDisconnectTimer() {
        // cancel any pending timer events
        if (disconnectTimer != null) {
            disconnectTimer.cancel();
            disconnectTimer.purge();
            disconnectTimer = null;
        }
    }

    /**
     * 创建一个新的会话
     *
     * @param rdpConfig 配置参数
     * @param context   context
     * @return RdpSession
     */
    public static RdpSession createSession(RdpConfig rdpConfig, Context context) {
        RdpSession session = new RdpSession(LibFreeRDP.newInstance(context), rdpConfig);
        sessionMap.put(session.getInstance(), session);
        return session;
    }


    /**
     * 创建一个新的会话
     *
     * @param openUri openUri
     * @param context context
     * @return RdpSession
     */
    public static RdpSession createSession(Uri openUri, Context context) {
        RdpSession session = new RdpSession(LibFreeRDP.newInstance(context), openUri);
        sessionMap.put(session.getInstance(), session);
        return session;
    }

    @Nullable
    public static RdpSession getSession(long instance) {
        return sessionMap.get(instance);
    }

    public static Collection<RdpSession> getSessions() {
        // 返回会话项的副本
        return new ArrayList<>(sessionMap.values());
    }

    public static void freeSession(long instance) {
        if (RdpApp.sessionMap.containsKey(instance)) {
            RdpApp.sessionMap.remove(instance);
            LibFreeRDP.freeInstance(instance);
        }
    }


    /**
     * LibFreeRDP.EventListener 的实现 =============================================================
     */
    @Override
    public void onPreConnect(long instance) {
        sendRdpNotification(FREERDP_EVENT_PREPARE_CONNECT, instance);
    }

    @Override
    public void onConnectionSuccess(long instance) {
        sendRdpNotification(FREERDP_EVENT_CONNECTION_SUCCESS, instance);
    }

    @Override
    public void onConnectionFailure(long instance) {
        sendRdpNotification(FREERDP_EVENT_CONNECTION_FAILURE, instance);
    }

    @Override
    public void onDisconnecting(long instance) {
        sendRdpNotification(FREERDP_EVENT_DISCONNECTING, instance);
    }

    @Override
    public void onDisconnected(long instance) {
        sendRdpNotification(FREERDP_EVENT_DISCONNECTED, instance);
    }

    /**
     * 发送 Rdp 状态广播
     *
     * @param type     类型
     * @param instance 实例
     */
    public static void sendRdpNotification(int type, long instance) {
        Intent intent = new Intent(ACTION_EVENT_FREERDP);
        intent.putExtra(EVENT_TYPE, type);
        intent.putExtra(EVENT_PARAM, instance);
        Utils.getApp().sendBroadcast(intent);
    }


    // TimerTask用于在屏幕关闭后断开会话
    private static class DisconnectTask extends TimerTask {
        @Override
        public void run() {
            Log.v("DisconnectTask", "Doing action");

            // 断开任何正在运行的rdp会话
            Collection<RdpSession> sessions = RdpApp.getSessions();
            for (RdpSession session : sessions) {
                LibFreeRDP.disconnect(session.getInstance());
            }
        }
    }
}
