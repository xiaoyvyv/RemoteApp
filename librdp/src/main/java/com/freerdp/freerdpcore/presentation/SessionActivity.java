package com.freerdp.freerdpcore.presentation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ZoomControls;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.freerdp.freerdpcore.application.RdpApp;
import com.freerdp.freerdpcore.domain.RdpConfig;
import com.freerdp.freerdpcore.domain.RdpSession;
import com.freerdp.freerdpcore.mapper.RdpKeyboardMapper;
import com.freerdp.freerdpcore.services.LibFreeRDP;
import com.freerdp.freerdpcore.services.UiEventListener;
import com.freerdp.freerdpcore.utils.ClipboardManagerProxy;
import com.freerdp.freerdpcore.utils.Mouse;
import com.freerdp.freerdpcore.view.RdpPointerView;
import com.freerdp.freerdpcore.view.RdpScrollView;
import com.freerdp.freerdpcore.view.RdpSessionView;
import com.xiaoyv.librdp.R;

import java.util.Collection;
import java.util.List;

public class SessionActivity extends AppCompatActivity
        implements UiEventListener, KeyboardView.OnKeyboardActionListener,
        RdpScrollView.ScrollView2DListener, RdpKeyboardMapper.KeyProcessingListener,
        RdpSessionView.SessionViewListener, RdpPointerView.TouchPointerListener,
        ClipboardManagerProxy.OnClipboardChangedListener {
    public static final String PARAM_CONNECTION_REFERENCE = "conRef";
    public static final String PARAM_INSTANCE = "instance";
    private static final float ZOOMING_STEP = 0.5f;
    private static final int ZOOMCONTROLS_AUTOHIDE_TIMEOUT = 4000;
    // timeout between subsequent scrolling requests when the touch-pointer is
    // at the edge of the session view
    private static final int SCROLLING_TIMEOUT = 50;
    private static final int SCROLLING_DISTANCE = 20;
    private static final String TAG = "FreeRDP.SessionActivity";
    // variables for delayed move event sending
    private static final int MAX_DISCARDED_MOVE_EVENTS = 3;
    private static final int SEND_MOVE_EVENT_TIMEOUT = 150;
    private Bitmap bitmap;
    private RdpSession session;
    private RdpSessionView rdpSessionView;
    private RdpPointerView rdpPointerView;
    private ProgressDialog progressDialog;
    private KeyboardView keyboardView;
    private KeyboardView modifiersKeyboardView;
    private ZoomControls zoomControls;
    private RdpKeyboardMapper rdpKeyboardMapper;

    private Keyboard specialkeysKeyboard;
    private Keyboard numpadKeyboard;
    private Keyboard cursorKeyboard;
    private Keyboard modifiersKeyboard;

    private AlertDialog dlgVerifyCertificate;
    private AlertDialog dlgUserCredentials;
    private View userCredView;

    private UIHandler uiHandler;

    private int screen_width;
    private int screen_height;

    private boolean connectCancelledByUser = false;
    private boolean sessionRunning = false;
    private boolean toggleMouseButtons = false;

    private LibFreeRDPBroadcastReceiver libFreeRDPBroadcastReceiver;
    private RdpScrollView scrollView;
    // keyboard visibility flags
    private boolean sysKeyboardVisible = false;
    private boolean extKeyboardVisible = false;
    private int discardedMoveEvents = 0;
    private ClipboardManagerProxy mClipboardManager;
    private boolean callbackDialogResult;
    View mDecor;

    private void createDialogs() {
        // build verify certificate dialog
        dlgVerifyCertificate =
                new AlertDialog.Builder(this)
                        .setTitle(R.string.dlg_title_verify_certificate)
                        .setPositiveButton(android.R.string.yes,
                                (dialog, which) -> {
                                    callbackDialogResult = true;
                                    synchronized (dialog) {
                                        dialog.notify();
                                    }
                                })
                        .setNegativeButton(android.R.string.no,
                                (dialog, which) -> {
                                    callbackDialogResult = false;
                                    connectCancelledByUser = true;
                                    synchronized (dialog) {
                                        dialog.notify();
                                    }
                                })
                        .setCancelable(false)
                        .create();

        // build the dialog
        userCredView = getLayoutInflater().inflate(R.layout.credentials, null, true);
        dlgUserCredentials =
                new AlertDialog.Builder(this)
                        .setView(userCredView)
                        .setTitle(R.string.dlg_title_credentials)
                        .setPositiveButton(android.R.string.ok,
                                (dialog, which) -> {
                                    callbackDialogResult = true;
                                    synchronized (dialog) {
                                        dialog.notify();
                                    }
                                })
                        .setNegativeButton(android.R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        callbackDialogResult = false;
                                        connectCancelledByUser = true;
                                        synchronized (dialog) {
                                            dialog.notify();
                                        }
                                    }
                                })
                        .setCancelable(false)
                        .create();
    }

    private boolean hasHardwareMenuButton() {
        boolean rc = false;
        final ViewConfiguration cfg = ViewConfiguration.get(this);
        return cfg.hasPermanentMenuKey();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // show status bar or make fullscreen?
        if (ApplicationSettingsActivity.getHideStatusBar(this)) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        this.setContentView(R.layout.session);
        if (hasHardwareMenuButton() || ApplicationSettingsActivity.getHideActionBar(this)) {
            this.getSupportActionBar().hide();
        } else {
            this.getSupportActionBar().show();
        }

        Log.v(TAG, "Session.onCreate");

        // ATTENTION: We use the onGlobalLayout notification to start our
        // session.
        // This is because only then we can know the exact size of our session
        // when using fit screen
        // accounting for any status bars etc. that Android might throws on us.
        // A bit weird looking
        // but this is the only way ...
        final View activityRootView = findViewById(R.id.session_root_view);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            screen_width = activityRootView.getWidth();
            screen_height = activityRootView.getHeight();

            // start session
            if (!sessionRunning && getIntent() != null) {
                processIntent(getIntent());
                sessionRunning = true;
            }
        });

        rdpSessionView = (RdpSessionView) findViewById(R.id.sessionView);
        rdpSessionView.setScaleGestureDetector(
                new ScaleGestureDetector(this, new PinchZoomListener()));
        rdpSessionView.setSessionViewListener(this);
        rdpSessionView.requestFocus();

        rdpPointerView = (RdpPointerView) findViewById(R.id.touchPointerView);
        rdpPointerView.setTouchPointerListener(this);

        rdpKeyboardMapper = new RdpKeyboardMapper();
        rdpKeyboardMapper.init(this);
        rdpKeyboardMapper.reset(this);

        modifiersKeyboard = new Keyboard(getApplicationContext(), R.xml.modifiers_keyboard);
        specialkeysKeyboard = new Keyboard(getApplicationContext(), R.xml.specialkeys_keyboard);
        numpadKeyboard = new Keyboard(getApplicationContext(), R.xml.numpad_keyboard);
        cursorKeyboard = new Keyboard(getApplicationContext(), R.xml.cursor_keyboard);

        // hide keyboard below the sessionView
        keyboardView = (KeyboardView) findViewById(R.id.extended_keyboard);
        keyboardView.setKeyboard(specialkeysKeyboard);
        keyboardView.setOnKeyboardActionListener(this);

        modifiersKeyboardView = (KeyboardView) findViewById(R.id.extended_keyboard_header);
        modifiersKeyboardView.setKeyboard(modifiersKeyboard);
        modifiersKeyboardView.setOnKeyboardActionListener(this);

        scrollView = findViewById(R.id.sessionScrollView);
        scrollView.setScrollViewListener(this);
        uiHandler = new UIHandler();
        libFreeRDPBroadcastReceiver = new LibFreeRDPBroadcastReceiver();

        zoomControls = findViewById(R.id.zoomControls);
        zoomControls.hide();
        zoomControls.setOnZoomInClickListener(v -> {
            resetZoomControlsAutoHideTimeout();
            zoomControls.setIsZoomInEnabled(rdpSessionView.zoomIn(ZOOMING_STEP));
            zoomControls.setIsZoomOutEnabled(true);
        });
        zoomControls.setOnZoomOutClickListener(v -> {
            resetZoomControlsAutoHideTimeout();
            zoomControls.setIsZoomOutEnabled(rdpSessionView.zoomOut(ZOOMING_STEP));
            zoomControls.setIsZoomInEnabled(true);
        });

        toggleMouseButtons = false;

        createDialogs();

        // register freerdp events broadcast receiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(RdpApp.ACTION_EVENT_FREERDP);
        registerReceiver(libFreeRDPBroadcastReceiver, filter);

        mClipboardManager = ClipboardManagerProxy.getClipboardManager(this);
        mClipboardManager.addClipboardChangedListener(this);

        mDecor = getWindow().getDecorView();
        mDecor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "Session.onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(TAG, "Session.onRestart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "Session.onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.v(TAG, "Session.onPause");

        // hide any visible keyboards
        showKeyboard(false, false);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "Session.onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "Session.onDestroy");

        // Cancel running disconnect timers.
        RdpApp.cancelDisconnectTimer();

        // Disconnect all remaining sessions.
        Collection<RdpSession> sessions = RdpApp.getSessions();
        for (RdpSession session : sessions)
            LibFreeRDP.disconnect(session.getInstance());

        // unregister freerdp events broadcast receiver
        unregisterReceiver(libFreeRDPBroadcastReceiver);

        // remove clipboard listener
        mClipboardManager.removeClipboardboardChangedListener(this);

        // free session
        RdpApp.freeSession(session.getInstance());

        session = null;
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // reload keyboard resources (changed from landscape)
        modifiersKeyboard = new Keyboard(getApplicationContext(), R.xml.modifiers_keyboard);
        specialkeysKeyboard = new Keyboard(getApplicationContext(), R.xml.specialkeys_keyboard);
        numpadKeyboard = new Keyboard(getApplicationContext(), R.xml.numpad_keyboard);
        cursorKeyboard = new Keyboard(getApplicationContext(), R.xml.cursor_keyboard);

        // apply loaded keyboards
        keyboardView.setKeyboard(specialkeysKeyboard);
        modifiersKeyboardView.setKeyboard(modifiersKeyboard);

        mDecor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void processIntent(Intent intent) {
        // get either session instance or create one from a bookmark/uri
        Bundle bundle = Bundle.EMPTY;
        Uri openUri = intent.getData();
        if (openUri != null) {
            // Launched from URI, e.g:
            // freerdp://user@ip:port/connect?sound=&rfx=&p=password&clipboard=%2b&themes=-
            connect(openUri);
        } else if (bundle.containsKey(PARAM_INSTANCE)) {
            int inst = bundle.getInt(PARAM_INSTANCE);
            session = RdpApp.getSession(inst);
            bitmap = session.getSurface().getBitmap();
            bindSession();
        } else {
            // no session found - exit
//            closeSessionActivity(RESULT_CANCELED);
            connect(new RdpConfig());
        }
    }

    private void connect(RdpConfig rdpConfig) {
        session = RdpApp.createSession(rdpConfig, getApplicationContext());

        RdpConfig.ActiveScreenSettings screenSettings =
                session.getRdpConfig().getScreenSettings();
        Log.v(TAG, "Screen Resolution: " + screenSettings.getResolutionString());
        if (screenSettings.isAutomatic()) {
            if ((getResources().getConfiguration().screenLayout &
                    Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE) {
                // large screen device i.e. tablet: simply use screen info
                screenSettings.setHeight(screen_height);
                screenSettings.setWidth(screen_width);
            } else {
                // small screen device i.e. phone:
                // Automatic uses the largest side length of the screen and
                // makes a 16:10 resolution setting out of it
                int screenMax = Math.max(screen_width, screen_height);
                screenSettings.setHeight(screenMax);
                screenSettings.setWidth((int) ((float) screenMax * 1.6f));
            }
        }
        if (screenSettings.isFitScreen()) {
            screenSettings.setHeight(screen_height);
            screenSettings.setWidth(screen_width);
        }

        connectWithTitle(rdpConfig.getLabel());
    }

    private void connect(Uri openUri) {
        session = RdpApp.createSession(openUri, getApplicationContext());

        connectWithTitle(openUri.getAuthority());
    }

    private void connectWithTitle(String title) {
        session.setUiEventListener(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle(title);
        progressDialog.setMessage(getResources().getText(R.string.dlg_msg_connecting));
        progressDialog.setButton(
                ProgressDialog.BUTTON_NEGATIVE, "Cancel", (dialog, which) -> {
                    connectCancelledByUser = true;
                    LibFreeRDP.cancelConnection(session.getInstance());
                });
        progressDialog.setCancelable(false);
        progressDialog.show();


        ThreadUtils.getCachedPool().submit(() -> session.connect());
    }

    /**
     * 通过将当前会话与sessionView连接起来并相应地更新所有内部对象，从而将当前会话绑定到活动
     */
    private void bindSession() {
        Log.v(TAG, "bindSession called");
        session.setUiEventListener(this);
        rdpSessionView.onSurfaceChange(session);
        scrollView.requestLayout();
        rdpKeyboardMapper.reset(this);
        mDecor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void hideSoftInput() {
        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (mgr.isActive()) {
            mgr.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS, 0);
        } else {
            mgr.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    // 显示系统或扩展键盘或不显示
    private void showKeyboard(final boolean showSystemKeyboard, final boolean showExtendedKeyboard) {
        // 无论我们在做什么...隐藏缩放控件
        // TODO：这无法正常工作，因为隐藏了键盘问题
        // 再次显示控件的 onScrollChange 通知...
        uiHandler.removeMessages(UIHandler.HIDE_ZOOMCONTROLS);
        if (zoomControls.getVisibility() == View.VISIBLE)
            zoomControls.hide();

        InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (showSystemKeyboard) {
            // 隐藏扩展键盘
            keyboardView.setVisibility(View.GONE);
            // 显示系统键盘
            mgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

            // 显示修改器键盘
            modifiersKeyboardView.setVisibility(View.VISIBLE);
        } else if (showExtendedKeyboard) {
            // 隐藏系统键盘
            hideSoftInput();

            // 显示扩展键盘
            keyboardView.setKeyboard(specialkeysKeyboard);
            keyboardView.setVisibility(View.VISIBLE);
            modifiersKeyboardView.setVisibility(View.VISIBLE);
        } else {
            // 隐藏两者
            hideSoftInput();
            keyboardView.setVisibility(View.GONE);
            modifiersKeyboardView.setVisibility(View.GONE);

            // 清除所有活动的按键修饰符
            rdpKeyboardMapper.clearAllModifiers();
        }

        sysKeyboardVisible = showSystemKeyboard;
        extKeyboardVisible = showExtendedKeyboard;
    }

    private void closeSessionActivity(int resultCode) {
        // 返回主页（并将意图数据发送回主页）
        setResult(resultCode, getIntent());
        finish();
    }

    // 更新我们的修改键的状态
    private void updateModifierKeyStates() {
        // 检查键码列表中是否有任何键

        List<Keyboard.Key> keys = modifiersKeyboard.getKeys();
        for (Keyboard.Key curKey : keys) {
            // if the key is a sticky key - just set it to off
            if (curKey.sticky) {
                switch (rdpKeyboardMapper.getModifierState(curKey.codes[0])) {
                    case RdpKeyboardMapper.KEYSTATE_ON:
                        curKey.on = true;
                        curKey.pressed = false;
                        break;

                    case RdpKeyboardMapper.KEYSTATE_OFF:
                        curKey.on = false;
                        curKey.pressed = false;
                        break;

                    case RdpKeyboardMapper.KEYSTATE_LOCKED:
                        curKey.on = true;
                        curKey.pressed = true;
                        break;
                }
            }
        }

        // refresh image
        modifiersKeyboardView.invalidateAllKeys();
    }

    private void sendDelayedMoveEvent(int x, int y) {
        if (uiHandler.hasMessages(UIHandler.SEND_MOVE_EVENT)) {
            uiHandler.removeMessages(UIHandler.SEND_MOVE_EVENT);
            discardedMoveEvents++;
        } else
            discardedMoveEvents = 0;

        if (discardedMoveEvents > MAX_DISCARDED_MOVE_EVENTS) {
            LibFreeRDP.sendCursorEvent(session.getInstance(), x, y, Mouse.getMoveEvent());
        } else {
            uiHandler.sendMessageDelayed(Message.obtain(null, UIHandler.SEND_MOVE_EVENT, x, y),
                    SEND_MOVE_EVENT_TIMEOUT);
        }
    }

    private void cancelDelayedMoveEvent() {
        uiHandler.removeMessages(UIHandler.SEND_MOVE_EVENT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.session_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // refer to http://tools.android.com/tips/non-constant-fields why we
        // can't use switch/case here ..
        int itemId = item.getItemId();

        if (itemId == R.id.session_touch_pointer) {
            // toggle touch pointer
            if (rdpPointerView.getVisibility() == View.VISIBLE) {
                rdpPointerView.setVisibility(View.INVISIBLE);
                rdpSessionView.setTouchPointerPadding(0, 0);
            } else {
                rdpPointerView.setVisibility(View.VISIBLE);
                rdpSessionView.setTouchPointerPadding(rdpPointerView.getPointerWidth(),
                        rdpPointerView.getPointerHeight());
            }
        } else if (itemId == R.id.session_sys_keyboard) {
            showKeyboard(!sysKeyboardVisible, false);
        } else if (itemId == R.id.session_ext_keyboard) {
            showKeyboard(false, !extKeyboardVisible);
        } else if (itemId == R.id.session_disconnect) {
            showKeyboard(false, false);
            LibFreeRDP.disconnect(session.getInstance());
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        // hide keyboards (if any visible) or send alt+f4 to the session
        if (sysKeyboardVisible || extKeyboardVisible) {
            showKeyboard(false, false);
        } else {
            rdpKeyboardMapper.sendAltF4();
        }
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            LibFreeRDP.disconnect(session.getInstance());
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    // android keyboard input handling
    // We always use the unicode value to process input from the android
    // keyboard except if key modifiers
    // (like Win, Alt, Ctrl) are activated. In this case we will send the
    // virtual key code to allow key
    // combinations (like Win + E to open the explorer).
    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        ToastUtils.showShort(keycode);
        return rdpKeyboardMapper.processAndroidKeyEvent(event);
    }

    @Override
    public boolean onKeyUp(int keycode, KeyEvent event) {
        return rdpKeyboardMapper.processAndroidKeyEvent(event);
    }

    // onKeyMultiple is called for input of some special characters like umlauts
    // and some symbol characters
    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return rdpKeyboardMapper.processAndroidKeyEvent(event);
    }

    // ****************************************************************************
    // KeyboardView.KeyboardActionEventListener
    @Override
    public void onKey(int primaryCode, int[] keyCodes) {
        rdpKeyboardMapper.processCustomKeyEvent(primaryCode);
    }

    @Override
    public void onText(CharSequence text) {
    }

    @Override
    public void swipeRight() {
    }

    @Override
    public void swipeLeft() {
    }

    @Override
    public void swipeDown() {
    }

    @Override
    public void swipeUp() {
    }

    @Override
    public void onPress(int primaryCode) {
    }

    @Override
    public void onRelease(int primaryCode) {
    }

    // ****************************************************************************
    // KeyboardMapper.KeyProcessingListener implementation
    @Override
    public void processVirtualKey(int virtualKeyCode, boolean down) {
        LibFreeRDP.sendKeyEvent(session.getInstance(), virtualKeyCode, down);
    }

    @Override
    public void processUnicodeKey(int unicodeKey) {
        LibFreeRDP.sendUnicodeKeyEvent(session.getInstance(), unicodeKey, true);
        LibFreeRDP.sendUnicodeKeyEvent(session.getInstance(), unicodeKey, false);
    }

    @Override
    public void switchKeyboard(int keyboardType) {
        switch (keyboardType) {
            case RdpKeyboardMapper.KEYBOARD_TYPE_FUNCTIONKEYS:
                keyboardView.setKeyboard(specialkeysKeyboard);
                break;

            case RdpKeyboardMapper.KEYBOARD_TYPE_NUMPAD:
                keyboardView.setKeyboard(numpadKeyboard);
                break;

            case RdpKeyboardMapper.KEYBOARD_TYPE_CURSOR:
                keyboardView.setKeyboard(cursorKeyboard);
                break;

            default:
                break;
        }
    }

    @Override
    public void modifiersChanged() {
        updateModifierKeyStates();
    }

    // ****************************************************************************
    // LibFreeRDP UI event listener implementation
    @Override
    public void onSettingsChanged(int width, int height, int bpp) {

        if (bpp > 16)
            bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        else
            bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);

        session.setSurface(new BitmapDrawable(bitmap));

        if (session.getRdpConfig() == null) {
            // Return immediately if we launch from URI
            return;
        }

        // check this settings and initial settings - if they are not equal the
        // server doesn't support our settings
        // FIXME: the additional check (settings.getWidth() != width + 1) is for
        // the RDVH bug fix to avoid accidental notifications
        // (refer to android_freerdp.c for more info on this problem)
        RdpConfig.ActiveScreenSettings settings = session.getRdpConfig().getScreenSettings();
        if ((settings.getWidth() != width && settings.getWidth() != width + 1) ||
                settings.getHeight() != height || settings.getColors() != bpp)
            uiHandler.sendMessage(
                    Message.obtain(null, UIHandler.DISPLAY_TOAST,
                            getResources().getText(R.string.info_capabilities_changed)));
    }

    @Override
    public void onGraphicsUpdate(int x, int y, int width, int height) {
        LibFreeRDP.updateGraphics(session.getInstance(), bitmap, x, y, width, height);

        rdpSessionView.addInvalidRegion(new Rect(x, y, x + width, y + height));

        /*
         * since sessionView can only be modified from the UI thread any
         * modifications to it need to be scheduled
         */

        uiHandler.sendEmptyMessage(UIHandler.REFRESH_SESSIONVIEW);
    }

    @Override
    public void onGraphicsResize(int width, int height, int bpp) {
        // replace bitmap
        if (bpp > 16)
            bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        else
            bitmap = Bitmap.createBitmap(width, height, Config.RGB_565);
        session.setSurface(new BitmapDrawable(bitmap));

        /*
         * since sessionView can only be modified from the UI thread any
         * modifications to it need to be scheduled
         */
        uiHandler.sendEmptyMessage(UIHandler.GRAPHICS_CHANGED);
    }

    @Override
    public boolean onAuthenticate(StringBuilder username, StringBuilder domain,
                                  StringBuilder password) {
        // this is where the return code of our dialog will be stored
        callbackDialogResult = false;

        // set text fields
        ((EditText) userCredView.findViewById(R.id.editTextUsername)).setText(username);
        ((EditText) userCredView.findViewById(R.id.editTextDomain)).setText(domain);
        ((EditText) userCredView.findViewById(R.id.editTextPassword)).setText(password);

        // start dialog in UI thread
        uiHandler.sendMessage(Message.obtain(null, UIHandler.SHOW_DIALOG, dlgUserCredentials));

        // wait for result
        try {
            synchronized (dlgUserCredentials) {
                dlgUserCredentials.wait();
            }
        } catch (InterruptedException e) {
        }

        // clear buffers
        username.setLength(0);
        domain.setLength(0);
        password.setLength(0);

        // read back user credentials
        username.append(
                ((EditText) userCredView.findViewById(R.id.editTextUsername)).getText().toString());
        domain.append(
                ((EditText) userCredView.findViewById(R.id.editTextDomain)).getText().toString());
        password.append(
                ((EditText) userCredView.findViewById(R.id.editTextPassword)).getText().toString());

        return callbackDialogResult;
    }

    @Override
    public boolean onGatewayAuthenticate(StringBuilder username, StringBuilder domain,
                                         StringBuilder password) {
        // this is where the return code of our dialog will be stored
        callbackDialogResult = false;

        // set text fields
        ((EditText) userCredView.findViewById(R.id.editTextUsername)).setText(username);
        ((EditText) userCredView.findViewById(R.id.editTextDomain)).setText(domain);
        ((EditText) userCredView.findViewById(R.id.editTextPassword)).setText(password);

        // start dialog in UI thread
        uiHandler.sendMessage(Message.obtain(null, UIHandler.SHOW_DIALOG, dlgUserCredentials));

        // wait for result
        try {
            synchronized (dlgUserCredentials) {
                dlgUserCredentials.wait();
            }
        } catch (InterruptedException e) {
        }

        // clear buffers
        username.setLength(0);
        domain.setLength(0);
        password.setLength(0);

        // read back user credentials
        username.append(
                ((EditText) userCredView.findViewById(R.id.editTextUsername)).getText().toString());
        domain.append(
                ((EditText) userCredView.findViewById(R.id.editTextDomain)).getText().toString());
        password.append(
                ((EditText) userCredView.findViewById(R.id.editTextPassword)).getText().toString());

        return callbackDialogResult;
    }


    @Override
    public int onVerifyCertificateEx(@NonNull String host, long port, @NonNull String commonName, @NonNull String subject, @NonNull String issuer, @NonNull String fingerprint, long flags) {
        // see if global settings says accept all
        if (ApplicationSettingsActivity.getAcceptAllCertificates(this))
            return 0;

        // this is where the return code of our dialog will be stored
        callbackDialogResult = false;

        // set message
        String msg = getResources().getString(R.string.dlg_msg_verify_certificate);
        msg = msg + "\n\nSubject: " + subject + "\nIssuer: " + issuer +
                "\nFingerprint: " + fingerprint;
        dlgVerifyCertificate.setMessage(msg);

        // start dialog in UI thread
        uiHandler.sendMessage(Message.obtain(null, UIHandler.SHOW_DIALOG, dlgVerifyCertificate));

        // wait for result
        try {
            synchronized (dlgVerifyCertificate) {
                dlgVerifyCertificate.wait();
            }
        } catch (InterruptedException e) {
        }

        return callbackDialogResult ? 1 : 0;
    }

    @Override
    public int onVerifyChangedCertificateEx(@NonNull String host,
                                            long port,
                                            @NonNull String commonName,
                                            @NonNull String subject,
                                            @NonNull String issuer,
                                            @NonNull String fingerprint,
                                            @NonNull String oldSubject,
                                            @NonNull String oldIssuer,
                                            @NonNull String oldFingerprint,
                                            long flags) {
        // see if global settings says accept all
        if (ApplicationSettingsActivity.getAcceptAllCertificates(this))
            return 0;

        // this is where the return code of our dialog will be stored
        callbackDialogResult = false;

        // set message
        String msg = getResources().getString(R.string.dlg_msg_verify_certificate);
        msg = msg + "\n\nSubject: " + subject + "\nIssuer: " + issuer +
                "\nFingerprint: " + fingerprint;
        dlgVerifyCertificate.setMessage(msg);

        // start dialog in UI thread
        uiHandler.sendMessage(Message.obtain(null, UIHandler.SHOW_DIALOG, dlgVerifyCertificate));

        // wait for result
        try {
            synchronized (dlgVerifyCertificate) {
                dlgVerifyCertificate.wait();
            }
        } catch (InterruptedException e) {
        }

        return callbackDialogResult ? 1 : 0;
    }

    @Override
    public void onRemoteClipboardChanged(String data) {
        Log.v(TAG, "OnRemoteClipboardChanged: " + data);
        mClipboardManager.setLocalClipboardData(data);
    }

    // ****************************************************************************
    // ScrollView2DListener implementation
    private void resetZoomControlsAutoHideTimeout() {
        uiHandler.removeMessages(UIHandler.HIDE_ZOOMCONTROLS);
        uiHandler.sendEmptyMessageDelayed(UIHandler.HIDE_ZOOMCONTROLS,
                ZOOMCONTROLS_AUTOHIDE_TIMEOUT);
    }

    @Override
    public void onScrollChanged(RdpScrollView scrollView, int x, int y, int oldx, int oldy) {
        zoomControls.setIsZoomInEnabled(rdpSessionView.isNotAtMaxZoom());
        zoomControls.setIsZoomOutEnabled(rdpSessionView.isNotAtMinZoom());
        if (!ApplicationSettingsActivity.getHideZoomControls(this) &&
                zoomControls.getVisibility() != View.VISIBLE)
            zoomControls.show();
        resetZoomControlsAutoHideTimeout();
    }

    // ****************************************************************************
    // SessionView.SessionViewListener
    @Override
    public void onSessionViewBeginTouch() {
        scrollView.setScrollEnabled(false);
    }

    @Override
    public void onSessionViewEndTouch() {
        scrollView.setScrollEnabled(true);
    }

    @Override
    public void onSessionViewLeftTouch(int x, int y, boolean down) {
        if (!down) {
            cancelDelayedMoveEvent();
        }

        LibFreeRDP.sendCursorEvent(session.getInstance(), x, y,
                toggleMouseButtons ? Mouse.getRightButtonEvent(down)
                        : Mouse.getLeftButtonEvent(down));

        if (!down) {
            toggleMouseButtons = false;
        }
    }

    @Override
    public void onSessionViewRightTouch(int x, int y, boolean down) {
        if (!down) {
            toggleMouseButtons = !toggleMouseButtons;
        }
    }

    @Override
    public void onSessionViewMove(int x, int y) {
        sendDelayedMoveEvent(x, y);
    }

    @Override
    public void onSessionViewScroll(boolean down) {
        LibFreeRDP.sendCursorEvent(session.getInstance(), 0, 0, Mouse.getScrollEvent(down));
    }

    // ****************************************************************************
    // TouchPointerView.TouchPointerListener
    @Override
    public void onTouchPointerClose() {
        rdpPointerView.setVisibility(View.INVISIBLE);
        rdpSessionView.setTouchPointerPadding(0, 0);
    }

    private Point mapScreenCoordToSessionCoord(int x, int y) {
        int mappedX = (int) ((float) (x + scrollView.getScrollX()) / rdpSessionView.getZoom());
        int mappedY = (int) ((float) (y + scrollView.getScrollY()) / rdpSessionView.getZoom());
        if (mappedX > bitmap.getWidth())
            mappedX = bitmap.getWidth();
        if (mappedY > bitmap.getHeight())
            mappedY = bitmap.getHeight();
        return new Point(mappedX, mappedY);
    }

    @Override
    public void onTouchPointerLeftClick(int x, int y, boolean down) {
        Point p = mapScreenCoordToSessionCoord(x, y);
        LibFreeRDP.sendCursorEvent(session.getInstance(), p.x, p.y,
                Mouse.getLeftButtonEvent(down));
    }

    @Override
    public void onTouchPointerRightClick(int x, int y, boolean down) {
        Point p = mapScreenCoordToSessionCoord(x, y);
        LibFreeRDP.sendCursorEvent(session.getInstance(), p.x, p.y,
                Mouse.getRightButtonEvent(down));
    }

    @Override
    public void onTouchPointerMove(int x, int y) {
        Point p = mapScreenCoordToSessionCoord(x, y);
        LibFreeRDP.sendCursorEvent(session.getInstance(), p.x, p.y, Mouse.getMoveEvent());

        if (ApplicationSettingsActivity.getAutoScrollTouchPointer(this) &&
                !uiHandler.hasMessages(UIHandler.SCROLLING_REQUESTED)) {
            Log.v(TAG, "Starting auto-scroll");
            uiHandler.sendEmptyMessageDelayed(UIHandler.SCROLLING_REQUESTED, SCROLLING_TIMEOUT);
        }
    }

    @Override
    public void onTouchPointerScroll(boolean down) {
        LibFreeRDP.sendCursorEvent(session.getInstance(), 0, 0, Mouse.getScrollEvent(down));
    }

    @Override
    public void onTouchPointerToggleKeyboard() {
        showKeyboard(!sysKeyboardVisible, false);
    }

    @Override
    public void onTouchPointerToggleExtKeyboard() {
        showKeyboard(false, !extKeyboardVisible);
    }

    @Override
    public void onTouchPointerResetScrollZoom() {
        rdpSessionView.setZoom(1.0f);
        scrollView.scrollTo(0, 0);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent e) {
        super.onGenericMotionEvent(e);
        switch (e.getAction()) {
            case MotionEvent.ACTION_SCROLL:
                final float vScroll = e.getAxisValue(MotionEvent.AXIS_VSCROLL);
                if (vScroll < 0) {
                    LibFreeRDP.sendCursorEvent(session.getInstance(), 0, 0,
                            Mouse.getScrollEvent(false));
                }
                if (vScroll > 0) {
                    LibFreeRDP.sendCursorEvent(session.getInstance(), 0, 0,
                            Mouse.getScrollEvent(true));
                }
                break;
        }
        return true;
    }

    // ****************************************************************************
    // ClipboardManagerProxy.OnClipboardChangedListener
    @Override
    public void onLocalClipboardChanged(String data) {
        Log.v(TAG, "onClipboardChanged: " + data);
        LibFreeRDP.sendClipboardData(session.getInstance(), data);
    }

    private class UIHandler extends Handler {

        public static final int REFRESH_SESSIONVIEW = 1;
        public static final int DISPLAY_TOAST = 2;
        public static final int HIDE_ZOOMCONTROLS = 3;
        public static final int SEND_MOVE_EVENT = 4;
        public static final int SHOW_DIALOG = 5;
        public static final int GRAPHICS_CHANGED = 6;
        public static final int SCROLLING_REQUESTED = 7;

        UIHandler() {
            super();
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case GRAPHICS_CHANGED: {
                    rdpSessionView.onSurfaceChange(session);
                    scrollView.requestLayout();
                    break;
                }
                case REFRESH_SESSIONVIEW: {
                    rdpSessionView.invalidateRegion();
                    break;
                }
                case DISPLAY_TOAST: {
                    Toast errorToast = Toast.makeText(getApplicationContext(), msg.obj.toString(),
                            Toast.LENGTH_LONG);
                    errorToast.show();
                    break;
                }
                case HIDE_ZOOMCONTROLS: {
                    zoomControls.hide();
                    break;
                }
                case SEND_MOVE_EVENT: {
                    LibFreeRDP.sendCursorEvent(session.getInstance(), msg.arg1, msg.arg2,
                            Mouse.getMoveEvent());
                    break;
                }
                case SHOW_DIALOG: {
                    // create and show the dialog
                    ((Dialog) msg.obj).show();
                    break;
                }
                case SCROLLING_REQUESTED: {
                    int scrollX = 0;
                    int scrollY = 0;
                    float[] pointerPos = rdpPointerView.getPointerPosition();

                    if (pointerPos[0] > (screen_width - rdpPointerView.getPointerWidth()))
                        scrollX = SCROLLING_DISTANCE;
                    else if (pointerPos[0] < 0)
                        scrollX = -SCROLLING_DISTANCE;

                    if (pointerPos[1] > (screen_height - rdpPointerView.getPointerHeight()))
                        scrollY = SCROLLING_DISTANCE;
                    else if (pointerPos[1] < 0)
                        scrollY = -SCROLLING_DISTANCE;

                    scrollView.scrollBy(scrollX, scrollY);

                    // see if we reached the min/max scroll positions
                    if (scrollView.getScrollX() == 0 ||
                            scrollView.getScrollX() == (rdpSessionView.getWidth() - scrollView.getWidth()))
                        scrollX = 0;
                    if (scrollView.getScrollY() == 0 ||
                            scrollView.getScrollY() ==
                                    (rdpSessionView.getHeight() - scrollView.getHeight()))
                        scrollY = 0;

                    if (scrollX != 0 || scrollY != 0)
                        uiHandler.sendEmptyMessageDelayed(SCROLLING_REQUESTED, SCROLLING_TIMEOUT);
                    else
                        Log.v(TAG, "Stopping auto-scroll");
                    break;
                }
            }
        }
    }

    private class PinchZoomListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        private float scaleFactor = 1.0f;

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            scrollView.setScrollEnabled(false);
            return true;
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            // calc scale factor
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(RdpSessionView.MIN_SCALE_FACTOR,
                    Math.min(scaleFactor, RdpSessionView.MAX_SCALE_FACTOR));
            rdpSessionView.setZoom(scaleFactor);

            if (rdpSessionView.isNotAtMinZoom() && rdpSessionView.isNotAtMaxZoom()) {
                // transform scroll origin to the new zoom space
                float transOriginX = scrollView.getScrollX() * detector.getScaleFactor();
                float transOriginY = scrollView.getScrollY() * detector.getScaleFactor();

                // transform center point to the zoomed space
                float transCenterX =
                        (scrollView.getScrollX() + detector.getFocusX()) * detector.getScaleFactor();
                float transCenterY =
                        (scrollView.getScrollY() + detector.getFocusY()) * detector.getScaleFactor();

                // scroll by the difference between the distance of the
                // transformed center/origin point and their old distance
                // (focusX/Y)
                scrollView.scrollBy((int) ((transCenterX - transOriginX) - detector.getFocusX()),
                        (int) ((transCenterY - transOriginY) - detector.getFocusY()));
            }

            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector de) {
            scrollView.setScrollEnabled(true);
        }
    }

    private class LibFreeRDPBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            // still got a valid session?
            if (session == null)
                return;

            // is this event for the current session?
            if (session.getInstance() != intent.getExtras().getLong(RdpApp.EVENT_PARAM, -1))
                return;

            switch (intent.getExtras().getInt(RdpApp.EVENT_TYPE, -1)) {
                case RdpApp.FREERDP_EVENT_CONNECTION_SUCCESS:
                    OnConnectionSuccess(context);
                    break;

                case RdpApp.FREERDP_EVENT_CONNECTION_FAILURE:
                    OnConnectionFailure(context);
                    break;
                case RdpApp.FREERDP_EVENT_DISCONNECTED:
                    OnDisconnected(context);
                    break;
            }
        }

        private void OnConnectionSuccess(Context context) {
            Log.v(TAG, "OnConnectionSuccess");

            // bind session
            bindSession();

            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }
        }

        private void OnConnectionFailure(Context context) {
            Log.v(TAG, "OnConnectionFailure");

            // remove pending move events
            uiHandler.removeMessages(UIHandler.SEND_MOVE_EVENT);

            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            // post error message on UI thread
            if (!connectCancelledByUser)
                uiHandler.sendMessage(
                        Message.obtain(null, UIHandler.DISPLAY_TOAST,
                                getResources().getText(R.string.error_connection_failure)));

            closeSessionActivity(RESULT_CANCELED);
        }

        private void OnDisconnected(Context context) {
            Log.v(TAG, "OnDisconnected");

            // remove pending move events
            uiHandler.removeMessages(UIHandler.SEND_MOVE_EVENT);

            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
            }

            session.setUiEventListener(null);
            closeSessionActivity(RESULT_OK);
        }
    }
}
