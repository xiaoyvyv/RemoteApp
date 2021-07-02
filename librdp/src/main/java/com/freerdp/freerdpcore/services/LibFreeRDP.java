package com.freerdp.freerdpcore.services;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import androidx.collection.LongSparseArray;

import com.freerdp.freerdpcore.application.RdpApp;
import com.freerdp.freerdpcore.application.RdpSessionState;
import com.freerdp.freerdpcore.domain.BaseRdpBookmark;
import com.freerdp.freerdpcore.domain.RdpBookmark;
import com.freerdp.freerdpcore.presentation.ApplicationSettingsActivity;

import java.util.ArrayList;
import java.util.Locale;

public class LibFreeRDP {
    private static final String TAG = "LibFreeRDP";
    private static EventListener listener;
    private static boolean mHasH264 = true;

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

    private static String addFlag(String name, boolean enabled) {
        if (enabled) {
            return "+" + name;
        }
        return "-" + name;
    }

    /**
     * 配置连接信息
     *
     * @param context  context
     * @param inst     实例ID
     * @param bookmark bookmark
     * @return 是否成功
     */
    public static boolean setConnectionInfo(Context context, long inst, BaseRdpBookmark bookmark) {
        BaseRdpBookmark.ScreenSettings screenSettings = bookmark.getActiveScreenSettings();
        BaseRdpBookmark.AdvancedSettings advanced = bookmark.getAdvancedSettings();
        BaseRdpBookmark.DebugSettings debug = bookmark.getDebugSettings();

        String arg;
        ArrayList<String> args = new ArrayList<String>();

        args.add(TAG);
        args.add("/gdi:sw");

        final String clientName = ApplicationSettingsActivity.getClientName(context);
        if (!clientName.isEmpty()) {
            args.add("/client-hostname:" + clientName);
        }
        String certName = "";
        if (bookmark.getType() != BaseRdpBookmark.TYPE_MANUAL) {
            return false;
        }

        int port = bookmark.<RdpBookmark>get().getPort();
        String hostname = bookmark.<RdpBookmark>get().getHostname();

        args.add("/v:" + hostname);
        args.add("/port:" + String.valueOf(port));

        arg = bookmark.getUsername();
        if (!arg.isEmpty()) {
            args.add("/u:" + arg);
        }
        arg = bookmark.getDomain();
        if (!arg.isEmpty()) {
            args.add("/d:" + arg);
        }
        arg = bookmark.getPassword();
        if (!arg.isEmpty()) {
            args.add("/p:" + arg);
        }

        args.add(String.format(Locale.CANADA, "/size:%dx%d", screenSettings.getWidth(), screenSettings.getHeight()));
        args.add("/bpp:" + String.valueOf(screenSettings.getColors()));

        if (advanced.getConsoleMode()) {
            args.add("/admin");
        }

        switch (advanced.getSecurity()) {
            case 3: // NLA
                args.add("/sec-nla");
                break;
            case 2: // TLS
                args.add("/sec-tls");
                break;
            case 1: // RDP
                args.add("/sec-rdp");
                break;
            default:
                break;
        }

        if (!certName.isEmpty()) {
            args.add("/cert-name:" + certName);
        }

        BaseRdpBookmark.PerformanceFlags flags = bookmark.getActivePerformanceFlags();
        if (flags.getRemoteFX()) {
            args.add("/rfx");
        }

        if (flags.getGfx()) {
            args.add("/gfx");
        }

        if (flags.getH264() && mHasH264) {
            args.add("/gfx:AVC444");
        }

        args.add(addFlag("wallpaper", flags.getWallpaper()));
        args.add(addFlag("window-drag", flags.getFullWindowDrag()));
        args.add(addFlag("menu-anims", flags.getMenuAnimations()));
        args.add(addFlag("themes", flags.getTheme()));
        args.add(addFlag("fonts", flags.getFontSmoothing()));
        args.add(addFlag("aero", flags.getDesktopComposition()));
        args.add(addFlag("glyph-cache", false));

        if (!advanced.getRemoteProgram().isEmpty()) {
            args.add("/shell:" + advanced.getRemoteProgram());
        }

        if (!advanced.getWorkDir().isEmpty()) {
            args.add("/shell-dir:" + advanced.getWorkDir());
        }

        args.add(addFlag("async-channels", debug.getAsyncChannel()));
        args.add(addFlag("async-input", debug.getAsyncInput()));
        args.add(addFlag("async-update", debug.getAsyncUpdate()));

        if (advanced.getRedirectSDCard()) {
            String path = android.os.Environment.getExternalStorageDirectory().getPath();
            args.add("/drive:sdcard," + path);
        }

        args.add("/clipboard");

        // Gateway enabled?
        if (bookmark.getType() == BaseRdpBookmark.TYPE_MANUAL &&
                bookmark.<RdpBookmark>get().getEnableGatewaySettings()) {
            RdpBookmark.GatewaySettings gateway =
                    bookmark.<RdpBookmark>get().getGatewaySettings();

            args.add(String.format(Locale.getDefault(), "/g:%s:%d", gateway.getHostname(), gateway.getPort()));

            arg = gateway.getUsername();
            if (!arg.isEmpty()) {
                args.add("/gu:" + arg);
            }
            arg = gateway.getDomain();
            if (!arg.isEmpty()) {
                args.add("/gd:" + arg);
            }
            arg = gateway.getPassword();
            if (!arg.isEmpty()) {
                args.add("/gp:" + arg);
            }
        }

		/* 0 ... local
		   1 ... remote
		   2 ... disable */
        args.add("/audio-mode:" + String.valueOf(advanced.getRedirectSound()));
        if (advanced.getRedirectSound() == 0) {
            args.add("/sound");
        }

        if (advanced.getRedirectMicrophone()) {
            args.add("/microphone");
        }

        args.add("/cert-ignore");
        args.add("/log-level:" + debug.getDebugLevel());
        String[] arrayArgs = args.toArray(new String[args.size()]);
        return freerdp_parse_arguments(inst, arrayArgs);
    }

    public static boolean setConnectionInfo(Context context, long inst, Uri openUri) {
        ArrayList<String> args = new ArrayList<>();

        // Parse URI from query string. Same key overwrite previous one
        // freerdp://user@ip:port/connect?sound=&rfx=&p=password&clipboard=%2b&themes=-

        // Now we only support Software GDI
        args.add(TAG);
        args.add("/gdi:sw");

        final String clientName = ApplicationSettingsActivity.getClientName(context);
        if (!clientName.isEmpty()) {
            args.add("/client-hostname:" + clientName);
        }

        // Parse hostname and port. Set to 'v' argument
        String hostname = openUri.getHost();
        int port = openUri.getPort();
        if (hostname != null) {
            hostname = hostname + ((port == -1) ? "" : (":" + String.valueOf(port)));
            args.add("/v:" + hostname);
        }

        String user = openUri.getUserInfo();
        if (user != null) {
            args.add("/u:" + user);
        }

        for (String key : openUri.getQueryParameterNames()) {
            String value = openUri.getQueryParameter(key);

            if (value.isEmpty()) {
                // Query: key=
                // To freerdp argument: /key
                args.add("/" + key);
            } else if (value.equals("-") || value.equals("+")) {
                // Query: key=- or key=+
                // To freerdp argument: -key or +key
                args.add(value + key);
            } else {
                // Query: key=value
                // To freerdp argument: /key:value
                if (key.equals("drive") && value.equals("sdcard")) {
                    // Special for sdcard redirect
                    String path = android.os.Environment.getExternalStorageDirectory().getPath();
                    value = "sdcard," + path;
                }

                args.add("/" + key + ":" + value);
            }
        }

        String[] arrayArgs = args.toArray(new String[args.size()]);
        return freerdp_parse_arguments(inst, arrayArgs);
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
        RdpSessionState s = RdpApp.getSession(inst);
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
        RdpSessionState s = RdpApp.getSession(inst);
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
        RdpSessionState session = RdpApp.getSession(inst);
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
        RdpSessionState s = RdpApp.getSession(inst);
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
        RdpSessionState s = RdpApp.getSession(inst);
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
        RdpSessionState s = RdpApp.getSession(inst);
        if (s == null) {
            return;
        }
        UiEventListener uiEventListener = s.getUiEventListener();
        if (uiEventListener != null) {
            uiEventListener.onGraphicsUpdate(x, y, width, height);
        }
    }

    private static void OnGraphicsResize(long inst, int width, int height, int bpp) {
        RdpSessionState s = RdpApp.getSession(inst);
        if (s == null) {
            return;
        }
        UiEventListener uiEventListener = s.getUiEventListener();
        if (uiEventListener != null) {
            uiEventListener.onGraphicsResize(width, height, bpp);
        }
    }

    private static void OnRemoteClipboardChanged(long inst, String data) {
        RdpSessionState s = RdpApp.getSession(inst);
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
