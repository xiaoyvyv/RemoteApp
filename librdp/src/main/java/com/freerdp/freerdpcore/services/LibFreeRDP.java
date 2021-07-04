package com.freerdp.freerdpcore.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.collection.LongSparseArray;

import com.blankj.utilcode.util.LogUtils;
import com.freerdp.freerdpcore.application.RdpApp;
import com.freerdp.freerdpcore.domain.RdpConfig;
import com.freerdp.freerdpcore.domain.RdpSession;

public class LibFreeRDP {
    public static final String TAG = "LibFreeRDP";
    public static boolean mHasH264 = true;
    private static EventListener listener;

    private static final LongSparseArray<Boolean> mInstanceState = new LongSparseArray<>();

    static {
        final String h264 = "openh264";
        final String[] libraries = {h264,
                "freerdp-openssl",
                "ssl",
                "crypto",
                "jpeg",
                "winpr3",
                "freerdp3",
                "freerdp-client3",
                "freerdp-android3"};
        final String LD_PATH = System.getProperty("java.library.path");

        for (String lib : libraries) {
            try {
                Log.v(TAG, "Trying to load library " + lib + " from LD_PATH: " + LD_PATH);
                System.loadLibrary(lib);
            } catch (UnsatisfiedLinkError e) {
                Log.e(TAG, "Failed to load library " + lib + ": " + e.toString());
                if (lib.equals(h264)) {
                    mHasH264 = false;
                }
            }
        }
    }

    public static boolean hasH264Support() {
        return mHasH264;
    }

    private static native String freerdp_get_jni_version();

    private static native String freerdp_get_version();

    private static native String freerdp_get_build_date();

    private static native String freerdp_get_build_revision();

    private static native String freerdp_get_build_config();

    private static native long freerdp_new(Context context);

    private static native void freerdp_free(long inst);

    private static native boolean freerdp_parse_arguments(long inst, String[] args);

    private static native boolean freerdp_connect(long inst);

    private static native boolean freerdp_disconnect(long inst);

    private static native boolean freerdp_update_graphics(long inst, Bitmap bitmap, int x, int y,
                                                          int width, int height);

    private static native boolean freerdp_send_cursor_event(long inst, int x, int y, int flags);

    private static native boolean freerdp_send_key_event(long inst, int keycode, boolean down);

    private static native boolean freerdp_send_unicodekey_event(long inst, int keycode,
                                                                boolean down);

    private static native boolean freerdp_send_clipboard_data(long inst, String data);

    private static native String freerdp_get_last_error_string(long inst);

    public static void setEventListener(EventListener l) {
        listener = l;
    }

    public static long newInstance(Context context) {
        return freerdp_new(context);
    }

    public static void freeInstance(long inst) {
        synchronized (mInstanceState) {
            if (mInstanceState.get(inst, false)) {
                freerdp_disconnect(inst);
            }
            while (mInstanceState.get(inst, false)) {
                try {
                    mInstanceState.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException();
                }
            }
        }
        freerdp_free(inst);
    }

    public static boolean connect(long inst) {
        synchronized (mInstanceState) {
            if (mInstanceState.get(inst, false)) {
                throw new RuntimeException("instance already connected");
            }
        }
        return freerdp_connect(inst);
    }

    public static boolean disconnect(long inst) {
        synchronized (mInstanceState) {
            if (mInstanceState.get(inst, false)) {
                return freerdp_disconnect(inst);
            }
            return true;
        }
    }

    public static boolean cancelConnection(long inst) {
        synchronized (mInstanceState) {
            if (mInstanceState.get(inst, false)) {
                return freerdp_disconnect(inst);
            }
            return true;
        }
    }

    public static String addFlag(String name, boolean enabled) {
        if (enabled) {
            return "+" + name;
        }
        return "-" + name;
    }

    /**
     * 配置连接信息
     *
     * @param inst      实例ID
     * @param rdpConfig bookmark
     * @return 是否成功
     */
    public static boolean setConnectionInfo(long inst, RdpConfig rdpConfig) {
        String[] arguments = LibRdpHelper.setConnectionInfo(rdpConfig);
        LogUtils.e("Rdp 连接参数", arguments);
        return freerdp_parse_arguments(inst, arguments);
    }

    public static boolean setConnectionInfo(long inst, Uri openUri) {
        String[] arguments = LibRdpHelper.setConnectionInfo(openUri);
        LogUtils.e("Rdp 连接参数", arguments);
        return freerdp_parse_arguments(inst, arguments);
    }

    public static boolean updateGraphics(long inst, Bitmap bitmap, int x, int y, int width,
                                         int height) {
        return freerdp_update_graphics(inst, bitmap, x, y, width, height);
    }

    public static boolean sendCursorEvent(long inst, int x, int y, int flags) {
        return freerdp_send_cursor_event(inst, x, y, flags);
    }

    public static boolean sendKeyEvent(long inst, int keycode, boolean down) {
        return freerdp_send_key_event(inst, keycode, down);
    }

    public static boolean sendUnicodeKeyEvent(long inst, int keycode, boolean down) {
        return freerdp_send_unicodekey_event(inst, keycode, down);
    }

    public static boolean sendClipboardData(long inst, String data) {
        return freerdp_send_clipboard_data(inst, data);
    }

    private static void OnConnectionSuccess(long inst) {
        if (listener != null) {
            listener.onConnectionSuccess(inst);
        }
        synchronized (mInstanceState) {
            mInstanceState.append(inst, true);
            mInstanceState.notifyAll();
        }
    }

    private static void OnConnectionFailure(long inst) {
        if (listener != null) {
            listener.onConnectionFailure(inst);
        }
        synchronized (mInstanceState) {
            mInstanceState.remove(inst);
            mInstanceState.notifyAll();
        }
    }

    private static void OnPreConnect(long inst) {
        if (listener != null) {
            listener.onPreConnect(inst);
        }
    }

    private static void OnDisconnecting(long inst) {
        if (listener != null) {
            listener.onDisconnecting(inst);
        }
    }

    private static void OnDisconnected(long inst) {
        if (listener != null) {
            listener.onDisconnected(inst);
        }
        synchronized (mInstanceState) {
            mInstanceState.remove(inst);
            mInstanceState.notifyAll();
        }
    }

    private static void OnSettingsChanged(long inst, int width, int height, int bpp) {
        RdpSession s = RdpApp.getSession(inst);
        if (s == null) {
            return;
        }
        UiEventListener uiEventListener = s.getUiEventListener();
        if (uiEventListener != null) {
            uiEventListener.onSettingsChanged(width, height, bpp);
        }
    }

    private static boolean OnAuthenticate(long inst, StringBuilder username, StringBuilder domain,
                                          StringBuilder password) {
        RdpSession s = RdpApp.getSession(inst);
        if (s == null) {
            return false;
        }
        UiEventListener uiEventListener = s.getUiEventListener();
        if (uiEventListener != null) {
            return uiEventListener.onAuthenticate(username, domain, password);
        }
        return false;
    }

    private static boolean OnGatewayAuthenticate(long inst, StringBuilder username,
                                                 StringBuilder domain, StringBuilder password) {
        RdpSession session = RdpApp.getSession(inst);
        if (session == null) {
            return false;
        }
        UiEventListener uiEventListener = session.getUiEventListener();
        if (uiEventListener != null) {
            return uiEventListener.onGatewayAuthenticate(username, domain, password);
        }
        return false;
    }

    private static int OnVerifyCertificate(long inst, String commonName, String subject,
                                           String issuer, String fingerprint, boolean hostMismatch) {
        RdpSession s = RdpApp.getSession(inst);
        if (s == null) {
            return 0;
        }
        UiEventListener uiEventListener = s.getUiEventListener();
        if (uiEventListener != null) {
            return uiEventListener.onVerifiyCertificate(commonName, subject, issuer, fingerprint,
                    hostMismatch);
        }
        return 0;
    }

    private static int OnVerifyChangedCertificate(long inst, String commonName, String subject,
                                                  String issuer, String fingerprint,
                                                  String oldSubject, String oldIssuer,
                                                  String oldFingerprint) {
        RdpSession s = RdpApp.getSession(inst);
        if (s == null) {
            return 0;
        }
        UiEventListener uiEventListener = s.getUiEventListener();
        if (uiEventListener != null) {
            return uiEventListener.onVerifyChangedCertificate(
                    commonName, subject, issuer, fingerprint, oldSubject, oldIssuer, oldFingerprint);
        }
        return 0;
    }

    private static void OnGraphicsUpdate(long inst, int x, int y, int width, int height) {
        RdpSession s = RdpApp.getSession(inst);
        if (s == null) {
            return;
        }
        UiEventListener uiEventListener = s.getUiEventListener();
        if (uiEventListener != null) {
            uiEventListener.onGraphicsUpdate(x, y, width, height);
        }
    }

    private static void OnGraphicsResize(long inst, int width, int height, int bpp) {
        RdpSession s = RdpApp.getSession(inst);
        if (s == null) {
            return;
        }
        UiEventListener uiEventListener = s.getUiEventListener();
        if (uiEventListener != null) {
            uiEventListener.onGraphicsResize(width, height, bpp);
        }
    }

    private static void OnRemoteClipboardChanged(long inst, String data) {
        RdpSession s = RdpApp.getSession(inst);
        if (s == null) {
            return;
        }
        UiEventListener uiEventListener = s.getUiEventListener();
        if (uiEventListener != null) {
            uiEventListener.onRemoteClipboardChanged(data);
        }
    }

    public static String getVersion() {
        return freerdp_get_version();
    }

    /**
     * EventListener
     */
    public interface EventListener {
        void onPreConnect(long instance);

        void onConnectionSuccess(long instance);

        void onConnectionFailure(long instance);

        void onDisconnecting(long instance);

        void onDisconnected(long instance);
    }

    /**
     * UiEventListener
     */
    public interface UiEventListener {
        void onSettingsChanged(int width, int height, int bpp);

        boolean onAuthenticate(StringBuilder username, StringBuilder domain,
                               StringBuilder password);

        boolean onGatewayAuthenticate(StringBuilder username, StringBuilder domain,
                                      StringBuilder password);

        int onVerifiyCertificate(String commonName, String subject, String issuer,
                                 String fingerprint, boolean mismatch);

        int onVerifyChangedCertificate(String commonName, String subject, String issuer,
                                       String fingerprint, String oldSubject, String oldIssuer,
                                       String oldFingerprint);

        void onGraphicsUpdate(int x, int y, int width, int height);

        void onGraphicsResize(int width, int height, int bpp);

        void onRemoteClipboardChanged(String data);
    }
}
