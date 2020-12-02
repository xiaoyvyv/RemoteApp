package com.xiaoyv.rdp.screen.view;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.inputmethodservice.Keyboard;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;

import com.blankj.utilcode.util.BarUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.freerdp.freerdpcore.application.GlobalApp;
import com.freerdp.freerdpcore.application.SessionState;
import com.freerdp.freerdpcore.domain.BookmarkBase;
import com.freerdp.freerdpcore.domain.ConnectionReference;
import com.freerdp.freerdpcore.domain.ManualBookmark;
import com.freerdp.freerdpcore.utils.ClipboardManagerProxy;
import com.xiaoyv.busines.base.BaseMvpActivity;
import com.xiaoyv.librdp.mapper.KeyboardMapper;
import com.xiaoyv.rdp.R;
import com.xiaoyv.rdp.databinding.RdpActivityScreenBinding;
import com.xiaoyv.rdp.screen.contract.ScreenContract;
import com.xiaoyv.rdp.screen.listener.PinchZoomListener;
import com.xiaoyv.rdp.screen.listener.RdpBroadcastReceiver;
import com.xiaoyv.rdp.screen.listener.RdpClipboardChangedListener;
import com.xiaoyv.rdp.screen.listener.RdpFreeScrollChangeListener;
import com.xiaoyv.rdp.screen.listener.RdpKeyProcessingListener;
import com.xiaoyv.rdp.screen.listener.RdpKeyboardActionListener;
import com.xiaoyv.rdp.screen.listener.RdpSessionViewListener;
import com.xiaoyv.rdp.screen.listener.RdpTouchPointerListener;
import com.xiaoyv.rdp.screen.listener.RdpUiEventListener;
import com.xiaoyv.rdp.screen.presenter.ScreenPresenter;

/**
 * ScreenView
 *
 * @author why
 * @since 2020/12/02
 **/
public class ScreenActivity extends BaseMvpActivity<ScreenContract.View, ScreenPresenter> implements ScreenContract.View {
    public static final String PARAM_CONNECTION_REFERENCE = "conRef";
    public static final String PARAM_INSTANCE = "instance";

    // 缩放步长
    public static final float ZOOMING_STEP = 0.5f;
    public static final long ZOOMING_AUTO_HIDE_TIME = 4000;

    private RdpActivityScreenBinding binding;

    // 屏幕宽高
    private int screenWidth = ScreenUtils.getScreenWidth();
    private int screenHeight = ScreenUtils.getScreenHeight();

    // 键盘映射器
    private KeyboardMapper keyboardMapper;
    private RdpKeyProcessingListener keyProcessingListener;

    // 键盘布局
    private Keyboard specialKeysKeyboard;
    private Keyboard numPadKeyboard;
    private Keyboard cursorKeyboard;
    private Keyboard modifiersKeyboard;

    private SessionState session;
    private RdpBroadcastReceiver rdpBroadcastReceiver;
    private ClipboardManagerProxy clipboardManagerProxy;

    private View mDecor;
    private Bitmap bitmap;

    @Override
    protected ScreenPresenter createPresenter() {
        return new ScreenPresenter();
    }

    @Override
    protected View createContentView() {
        binding = RdpActivityScreenBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        // 连接内容视图
        binding.svSession.setScaleGestureDetector(new ScaleGestureDetector(this, new PinchZoomListener(binding.fsvScroll, binding.svSession)));
        binding.svSession.setSessionViewListener(new RdpSessionViewListener());
        binding.svSession.requestFocus();

        // 模拟鼠标指针
        binding.tpvPointer.setTouchPointerListener(new RdpTouchPointerListener());

        // 按键映射
        keyProcessingListener = new RdpKeyProcessingListener();
        keyboardMapper = new KeyboardMapper();
        keyboardMapper.init(this);
        keyboardMapper.reset(keyProcessingListener);

        // 各个键盘
        modifiersKeyboard = new Keyboard(getApplicationContext(), R.xml.modifiers_keyboard);
        specialKeysKeyboard = new Keyboard(getApplicationContext(), R.xml.specialkeys_keyboard);
        numPadKeyboard = new Keyboard(getApplicationContext(), R.xml.numpad_keyboard);
        cursorKeyboard = new Keyboard(getApplicationContext(), R.xml.cursor_keyboard);

        // 特殊操作键
        binding.kvKeyboard.setKeyboard(specialKeysKeyboard);
        binding.kvKeyboard.setOnKeyboardActionListener(new RdpKeyboardActionListener());
        // 光标操作键
        binding.kvKeyboardHeader.setKeyboard(modifiersKeyboard);
        binding.kvKeyboardHeader.setOnKeyboardActionListener(new RdpKeyboardActionListener());

        // 2D滑动布局
        binding.fsvScroll.setScrollViewListener(new RdpFreeScrollChangeListener());


        // 缩放控件
        binding.zcControls.hide();
        binding.zcControls.setOnZoomInClickListener(v -> {
            binding.zcControls.setIsZoomInEnabled(binding.svSession.zoomIn(ZOOMING_STEP));
            binding.zcControls.setIsZoomOutEnabled(true);
            ThreadUtils.runOnUiThreadDelayed(() -> {
                if (binding.zcControls != null) {
                    binding.zcControls.hide();
                }
            }, ZOOMING_AUTO_HIDE_TIME);
        });
        binding.zcControls.setOnZoomOutClickListener(v -> {
            binding.zcControls.setIsZoomOutEnabled(binding.svSession.zoomOut(ZOOMING_STEP));
            binding.zcControls.setIsZoomInEnabled(true);
            ThreadUtils.runOnUiThreadDelayed(() -> {
                if (binding.zcControls != null) {
                    binding.zcControls.hide();
                }
            }, ZOOMING_AUTO_HIDE_TIME);
        });

        // 连接广播
        rdpBroadcastReceiver = new RdpBroadcastReceiver(session);
        // 注册RDP连接事件广播接收器
        IntentFilter filter = new IntentFilter(GlobalApp.ACTION_EVENT_FREERDP);
        registerReceiver(rdpBroadcastReceiver, filter);

        // 剪切板代理
        clipboardManagerProxy = ClipboardManagerProxy.getClipboardManager(this);
        clipboardManagerProxy.addClipboardChangedListener(new RdpClipboardChangedListener());

        mDecor = getWindow().getDecorView();
        mDecor.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void initIntentData(Intent intent, Bundle bundle) {

    }


    @Override
    protected void initData() {

    }

    protected void processIntent(Intent intent) {
        RdpUiEventListener uiEventListener = new RdpUiEventListener();

        Bundle bundle = intent.getExtras();
        Uri openUri = intent.getData();
        // 从URI启动，例如：
        // freerdp://user@ip:port/connect?sound=&rfx=&p=password&clipboard=%2b&themes=-
        if (openUri != null) {
            session = GlobalApp.createSession(openUri, getApplicationContext());
            session.setUIEventListener(uiEventListener);
            // 连接
            return;
        }
        // 从实例中启动
        if (bundle.containsKey(PARAM_INSTANCE)) {
            int paramInstance = bundle.getInt(PARAM_INSTANCE);
            session = GlobalApp.getSession(paramInstance);
            bitmap = session.getSurface().getBitmap();

            // 绑定
            session.setUIEventListener(uiEventListener);
            binding.svSession.onSurfaceChange(session);
            binding.fsvScroll.requestLayout();
            keyboardMapper.reset(keyProcessingListener);
            BarUtils.setNavBarVisibility(getWindow(), false);
            return;
        }
        // 从配置文件启动
        if (bundle.containsKey(PARAM_CONNECTION_REFERENCE)) {
            String refStr = bundle.getString(PARAM_CONNECTION_REFERENCE);

            BookmarkBase bookmark = null;
            if (ConnectionReference.isHostnameReference(refStr)) {
                bookmark = new ManualBookmark();
                bookmark.<ManualBookmark>get().setHostname(ConnectionReference.getHostname(refStr));
            } else if (ConnectionReference.isBookmarkReference(refStr)) {
                if (ConnectionReference.isManualBookmarkReference(refStr)) {
                    bookmark = GlobalApp.getManualBookmarkGateway().findById(ConnectionReference.getManualBookmarkId(refStr));
                }
            }

            if (bookmark != null) {
               // connect(bookmark);
            }
        }

        //找不到会话-退出
        setResult(Activity.RESULT_CANCELED, getIntent());
        finish();
    }


}
