package com.freerdp.freerdpcore.application;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.util.Log;

import com.blankj.utilcode.util.NetworkUtils;
import com.freerdp.freerdpcore.domain.BookmarkBase;
import com.freerdp.freerdpcore.presentation.ApplicationSettingsActivity;
import com.freerdp.freerdpcore.services.BookmarkDB;
import com.freerdp.freerdpcore.services.HistoryDB;
import com.freerdp.freerdpcore.services.LibFreeRDP;
import com.freerdp.freerdpcore.services.ManualBookmarkGateway;
import com.freerdp.freerdpcore.services.QuickConnectHistoryGateway;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class GlobalApp implements LibFreeRDP.EventListener {
    private static volatile GlobalApp globalApp;
    private static final String TAG = "GlobalApp";
    // 事件通知定义
    public static final String EVENT_TYPE = "EVENT_TYPE";
    public static final String EVENT_PARAM = "EVENT_PARAM";
    public static final String ACTION_EVENT_FREERDP = "com.freerdp.freerdp.event.freerdp";
    public static final int FREERDP_EVENT_CONNECTION_SUCCESS = 1;
    public static final int FREERDP_EVENT_CONNECTION_FAILURE = 2;
    public static final int FREERDP_EVENT_DISCONNECTED = 3;
    public static boolean ConnectedToMobileWork = false;
    public static Map<Long, SessionState> sessionMap;
    public static BookmarkDB bookmarkDB;
    public static ManualBookmarkGateway manualBookmarkGateway;

    public static HistoryDB historyDB;
    public static QuickConnectHistoryGateway quickConnectHistoryGateway;

    // 屏幕关闭后用于断开会话的计时器
    public static Timer disconnectTimer = null;
    public static Application application;

    private GlobalApp() {

    }

    public static void init(Application application) {
        GlobalApp.application = application;
        if (globalApp == null) {
            synchronized (GlobalApp.class) {
                if (globalApp == null) {
                    globalApp = new GlobalApp();
                }
            }
        }
    }

    public static ManualBookmarkGateway getManualBookmarkGateway() {
        return manualBookmarkGateway;
    }

    public static QuickConnectHistoryGateway getQuickConnectHistoryGateway() {
        return quickConnectHistoryGateway;
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

    // RDP session handling
    static public SessionState createSession(BookmarkBase bookmark, Context context) {
        SessionState session = new SessionState(LibFreeRDP.newInstance(context), bookmark);
        sessionMap.put(session.getInstance(), session);
        return session;
    }

    static public SessionState createSession(Uri openUri, Context context) {
        SessionState session = new SessionState(LibFreeRDP.newInstance(context), openUri);
        sessionMap.put(session.getInstance(), session);
        return session;
    }

    static public SessionState getSession(long instance) {
        return sessionMap.get(instance);
    }

    static public Collection<SessionState> getSessions() {
        // 返回会话项的副本
        return new ArrayList<>(sessionMap.values());
    }

    static public void freeSession(long instance) {
        if (GlobalApp.sessionMap.containsKey(instance)) {
            GlobalApp.sessionMap.remove(instance);
            LibFreeRDP.freeInstance(instance);
        }
    }

    public void onCreate(Application application) {


        /* 初始化首选项。 */
        ApplicationSettingsActivity.get(application);

        sessionMap = Collections.synchronizedMap(new HashMap<>());

        LibFreeRDP.setEventListener(this);

        bookmarkDB = new BookmarkDB(application);

        manualBookmarkGateway = new ManualBookmarkGateway(bookmarkDB);

        historyDB = new HistoryDB(application);
        quickConnectHistoryGateway = new QuickConnectHistoryGateway(historyDB);

        ConnectedToMobileWork = NetworkUtils.is5G() || NetworkUtils.is4G();

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        application.registerReceiver(new ScreenReceiver(), filter);
    }

    // 发送FreeRDP通知的助手
    private void sendRDPNotification(int type, long param) {
        // 发送广播
        Intent intent = new Intent(ACTION_EVENT_FREERDP);
        intent.putExtra(EVENT_TYPE, type);
        intent.putExtra(EVENT_PARAM, param);
        application.sendBroadcast(intent);
    }

    @Override
    public void OnPreConnect(long instance) {
        Log.v(TAG, "OnPreConnect");
    }

    // //////////////////////////////////////////////////////////////////////
    // Implementation of LibFreeRDP.EventListener
    public void OnConnectionSuccess(long instance) {
        Log.v(TAG, "OnConnectionSuccess");
        sendRDPNotification(FREERDP_EVENT_CONNECTION_SUCCESS, instance);
    }

    public void OnConnectionFailure(long instance) {
        Log.v(TAG, "OnConnectionFailure");

        // 向会话活动发送通知
        sendRDPNotification(FREERDP_EVENT_CONNECTION_FAILURE, instance);
    }

    public void OnDisconnecting(long instance) {
        Log.v(TAG, "OnDisconnecting");
    }

    public void OnDisconnected(long instance) {
        Log.v(TAG, "OnDisconnected");
        sendRDPNotification(FREERDP_EVENT_DISCONNECTED, instance);
    }

    // TimerTask用于在屏幕关闭后断开会话
    private static class DisconnectTask extends TimerTask {
        @Override
        public void run() {
            Log.v("DisconnectTask", "Doing action");

            // 断开任何正在运行的rdp会话
            Collection<SessionState> sessions = GlobalApp.getSessions();
            for (SessionState session : sessions) {
                LibFreeRDP.disconnect(session.getInstance());
            }
        }
    }
}
