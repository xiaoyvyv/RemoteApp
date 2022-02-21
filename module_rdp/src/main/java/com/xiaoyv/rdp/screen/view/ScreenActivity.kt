package com.xiaoyv.rdp.screen.view

import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Point
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.view.*
import com.blankj.utilcode.util.*
import com.freerdp.freerdpcore.application.RdpApp
import com.freerdp.freerdpcore.domain.RdpConfig
import com.freerdp.freerdpcore.domain.RdpSession
import com.freerdp.freerdpcore.mapper.RdpKeyboardMapper
import com.freerdp.freerdpcore.services.LibFreeRDP
import com.freerdp.freerdpcore.services.LibRdpUiEventListener
import com.freerdp.freerdpcore.utils.ClipboardManagerProxy
import com.freerdp.freerdpcore.view.RdpPointerView
import com.freerdp.freerdpcore.view.RdpSessionView
import com.hijamoya.keyboardview.Keyboard
import com.xiaoyv.busines.base.BaseMvpActivity
import com.xiaoyv.busines.config.NavigationKey
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.rdp.R
import com.xiaoyv.rdp.databinding.RdpActivityScreenBinding
import com.xiaoyv.rdp.screen.config.RdpBroadcastReceiver
import com.xiaoyv.rdp.screen.config.RdpKeyboardActionListener
import com.xiaoyv.rdp.screen.config.RdpScreenZoomListener
import com.xiaoyv.rdp.screen.contract.ScreenContract
import com.xiaoyv.rdp.screen.presenter.ScreenPresenter
import com.xiaoyv.ui.scroll.FreeScrollView
import me.jessyan.autosize.internal.CancelAdapt


/**
 * ScreenView
 *
 * @author why
 * @since 2020/12/02
 */
class ScreenActivity : BaseMvpActivity<ScreenContract.View, ScreenPresenter>(),
    ScreenContract.View, LibRdpUiEventListener, RdpSessionView.SessionViewListener,
    FreeScrollView.ScrollView2DListener, RdpPointerView.TouchPointerListener, CancelAdapt,
    ClipboardManagerProxy.OnClipboardChangedListener, RdpKeyboardMapper.KeyProcessingListener,
    RdpKeyboardActionListener {
    private lateinit var binding: RdpActivityScreenBinding
    private lateinit var loading: ScreenLoadingFragment
    private lateinit var clipboardManager: ClipboardManagerProxy
    private lateinit var rdpKeyboardMapper: RdpKeyboardMapper

    private lateinit var modifiersKeyboard: Keyboard
    private lateinit var cursorKeyboard: Keyboard
    private lateinit var numberPadKeyboard: Keyboard
    private lateinit var specialKeyboard: Keyboard

    private val dlgVerifyCertificateLock = Object()
    private val dlgUserCredentialsLock = Object()
    private var rdpEntity: RdpEntity? = null
    private var rdpConfig: RdpConfig? = null
    private var callbackDialogResult = false

    /**
     * 是否由用户取消连接
     */
    private var connectCancelledByUser = false

    /**
     * 手势操作是否交换鼠标左右键
     */
    private var toggleMouseButtons = false
    private var screenLandscape = false
    private var connectSuccess = false

    /**
     * Rdp
     */
    private var rdpUri: Uri? = null
    private var rdpInstance: Long = -1
    private var rdpBitmap: Bitmap? = null

    /**
     * 根布局宽高
     */
    private var rootViewWidth = 0
    private var rootViewHeight = 0

    /**
     * Rdp 事件监听
     */
    private val rdpEventReceiver = RdpBroadcastReceiver().apply {
        /**
         * 连接成功
         */
        onConnectionSuccess = {
            connectSuccess = true

            presenter.v2pGetSession { session ->
                p2vBindSession(session)
                rdpKeyboardMapper.reset(this@ScreenActivity)
            }
            p2vHideLoading()
        }
        /**
         * 连接异常
         */
        onConnectionFailure = {
            connectSuccess = false

            presenter.v2pCancelDelayedMoveEvent()

            p2vHideLoading()
            vSessionClose(RESULT_CANCELED)
        }

        /**
         * 连接正常退出
         */
        onDisconnected = {
            connectSuccess = false

            presenter.v2pCancelDelayedMoveEvent()
            presenter.v2pGetSession { session ->
                session.libRdpUiEventListener = null
            }

            p2vHideLoading()
            vSessionClose(RESULT_OK)
        }
    }

    /**
     * 软键盘打开关闭监听
     */
    private val keyBoardListener = KeyboardUtils.OnSoftInputChangedListener {
        if (it > 0) {
            vOnSoftKeyBoardShow()
        } else {
            vOnSoftKeyBoardClose()
        }
    }

    override fun createPresenter(): ScreenPresenter {
        return ScreenPresenter()
    }

    override fun createContentView(): View {
        binding = RdpActivityScreenBinding.inflate(layoutInflater)
        return binding.root
    }


    override fun initIntentData(intent: Intent, bundle: Bundle) {
        rdpEntity = intent.getSerializableExtra(KEY_RDP_ENTITY) as? RdpEntity
        rdpInstance = intent.getLongExtra(KEY_RDP_INSTANCE, rdpInstance)
        rdpUri = intent.data
    }

    override fun initView() {
        binding.rsvSession.setScaleGestureDetector(
            ScaleGestureDetector(this, RdpScreenZoomListener(binding.rsvScroll, binding.rsvSession))
        )
        binding.rsvSession.setTouchPointerPadding(
            binding.tpvPointer.pointerWidth,
            binding.tpvPointer.pointerHeight
        )
        binding.rsvSession.requestFocus()
    }

    override fun initBar() {
        vSetLandscapeScreen(true)
    }

    /**
     * 设置横屏或竖屏
     */
    override fun vSetLandscapeScreen(landscape: Boolean) {
        screenLandscape = if (landscape) {
            ScreenUtils.setLandscape(this)
            true
        } else {
            ScreenUtils.setPortrait(this)
            false
        }
        hideSystemUi()
    }

    override fun initData() {
        // 注册 Rdp 事件广播接收器
        registerReceiver(rdpEventReceiver, IntentFilter(RdpApp.ACTION_EVENT_FREERDP))

        clipboardManager = ClipboardManagerProxy.getClipboardManager(this)

        modifiersKeyboard = Keyboard(applicationContext, R.xml.rdp_keyboard_modifiers)
        cursorKeyboard = Keyboard(applicationContext, R.xml.rdp_keyboard_cursor)
        numberPadKeyboard = Keyboard(applicationContext, R.xml.rdp_keyboard_numpad)
        specialKeyboard = Keyboard(applicationContext, R.xml.rdp_keyboard_specialkeys)

        // 扩展键盘
        binding.keyboardSpecial.keyboard = specialKeyboard
        binding.keyboardSpecial.setOnKeyboardActionListener(this)

        // 扩展修饰符键盘（ctrl|win|alt...）
        binding.keyboardHeader.keyboard = modifiersKeyboard
        binding.keyboardHeader.setOnKeyboardActionListener(this)

        rdpKeyboardMapper = RdpKeyboardMapper()
        rdpKeyboardMapper.init(this)
    }

    override fun initListener() {
        binding.root.doOnLayout {
            rootViewWidth = binding.root.width
            rootViewHeight = binding.root.height
        }

        binding.rsvSession.setSessionViewListener(this)
        binding.rsvScroll.setScrollViewListener(this)
        binding.tpvPointer.setTouchPointerListener(this)

        clipboardManager.addClipboardChangedListener(this)
        rdpKeyboardMapper.reset(this)

        // 注册软键盘打开关闭监听
        KeyboardUtils.registerSoftInputChangedListener(this, keyBoardListener)
    }

    /**
     * P层初始化完成，在这使用Presenter进行数据请求
     */
    override fun onPresenterCreated() {
        binding.root.post {
            // 在 UI 绘制完才开始准备连接，否则屏幕宽度高度信息不准确
            vProcessArgument()

            LogUtils.e(GsonUtils.toJson(rdpConfig))

            // 加载中对话框
            loading = ScreenLoadingFragment.Builder()
                .setTitle("远程桌面连接")
                .setLoadingTitle(String.format("正在连接到：\n%s", rdpConfig?.hostname))
                .setLoadingMessage("正在连接远程主机...")
                .setCancel {
                    callbackDialogResult = false
                    connectCancelledByUser = true
                    presenter.v2pGetSession { session ->
                        LibFreeRDP.cancelConnection(session.instance)
                    }
                }
                .build()
            p2vShowLoading()
        }
    }

    override fun vProcessArgument() {
        when {
            rdpUri != null -> {
                // 从 URI 启动
                // 例如: freerdp://user@ip:port/connect?sound=&rfx=&p=password&clipboard=%2b&themes=-
                presenter.v2pConnectWithUri(rdpUri!!)
            }
            rdpInstance != -1L -> {
                RdpApp.getSession(rdpInstance)?.let {
                    // 恢复上次实例
                    presenter.v2pStartConnect(it, true)
                } ?: vSessionClose(RESULT_CANCELED)
            }
            rdpEntity != null -> {
                // 获取配置信息
                rdpConfig =
                    GsonUtils.fromJson(rdpEntity!!.configStr.orEmpty(), RdpConfig::class.java)
                rdpConfig?.also {
                    presenter.v2pConnectWithConfig(it)
                } ?: vSessionClose(RESULT_CANCELED)
            }
            else -> {
                // 其他则结束对话
                vSessionClose(RESULT_CANCELED)
            }
        }
    }

    override fun p2vGetRootParam(): Pair<Int, Int> = Pair(rootViewWidth, rootViewHeight)

    override fun p2vScreenLandscape() = screenLandscape

    override fun p2vStartConnect(rdpSession: RdpSession): RdpSession {
        rdpSession.libRdpUiEventListener = this
        // 配置广播当前 session
        rdpEventReceiver.currentInstance = rdpSession.instance
        return rdpSession
    }

    override fun p2vBindSession(rdpSession: RdpSession) {
        binding.rsvSession.onSurfaceChange(rdpSession)
        binding.rsvSession.requestFocus()
        binding.rsvScroll.requestLayout()

        hideSystemUi()

        binding.tpvPointer.isVisible = true
    }

    override fun onScrollChanged(
        scrollView: FreeScrollView, x: Int, y: Int, oldx: Int, oldy: Int
    ) {

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // 重新加载键盘资源（从横向更改）
        modifiersKeyboard = Keyboard(applicationContext, R.xml.rdp_keyboard_modifiers)
        cursorKeyboard = Keyboard(applicationContext, R.xml.rdp_keyboard_cursor)
        numberPadKeyboard = Keyboard(applicationContext, R.xml.rdp_keyboard_numpad)
        specialKeyboard = Keyboard(applicationContext, R.xml.rdp_keyboard_specialkeys)

        // 应用加载的键盘
        binding.keyboardSpecial.keyboard = specialKeyboard
        binding.keyboardHeader.keyboard = modifiersKeyboard

        screenLandscape = newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE
        if (screenLandscape) {

        } else {

        }

        hideSystemUi()
    }

    private fun hideSystemUi() {
        val controller = ViewCompat.getWindowInsetsController(window.decorView)
        controller?.hide(WindowInsetsCompat.Type.statusBars())
        controller?.hide(WindowInsetsCompat.Type.navigationBars())
        // 导航栏隐藏时手势操作
        controller?.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // 刘海屏幕适配，允许使用刘海屏区进行绘制
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
    }


    /**
     * UiEventListener 回调 ========================================================================
     */
    override fun onAuthenticate(
        username: StringBuilder, domain: StringBuilder, password: StringBuilder
    ): Boolean {
        // 重置
        callbackDialogResult = false

        // 显示凭证验证对话框
        ThreadUtils.runOnUiThread {
            p2vHideLoading()

            ScreenCredentialsFragment.Builder()
                .setTitle("远程桌面连接-登录凭证验证")
                .setSubtitle("请输入您的登录凭据")
                .setCredentials("这些凭据将用于连接：" + rdpConfig?.hostname)
                .setData(username.toString(), password.toString(), domain.toString())
                .setCancel {
                    callbackDialogResult = false
                    connectCancelledByUser = true
                    synchronized(dlgUserCredentialsLock) {
                        dlgUserCredentialsLock.notify()
                    }
                }
                .setDone { u, p, d ->
                    // 清空
                    username.setLength(0)
                    domain.setLength(0)
                    password.setLength(0)

                    // 读取用户操作后的凭据
                    username.append(u)
                    domain.append(p)
                    password.append(d)

                    p2vShowLoading("正在配置远程会话...")

                    callbackDialogResult = true
                    synchronized(dlgUserCredentialsLock) {
                        dlgUserCredentialsLock.notify()
                    }
                }.build().show(supportFragmentManager)
        }
        // 等待验证结果
        try {
            synchronized(dlgUserCredentialsLock) {
                dlgUserCredentialsLock.wait()
            }
        } catch (e: InterruptedException) {
        }

        return callbackDialogResult
    }

    /**
     * 网关验证
     */
    override fun onGatewayAuthenticate(
        username: StringBuilder, domain: StringBuilder, password: StringBuilder
    ): Boolean {
        // 重置
        callbackDialogResult = false

        // 显示凭证验证对话框
        ThreadUtils.runOnUiThread {
            p2vHideLoading()

            ScreenCredentialsFragment.Builder()
                .setTitle("远程桌面连接-网关凭证验证")
                .setSubtitle("请输入您的网关凭据")
                .setCredentials("这些凭据将用于连接：" + rdpConfig?.gatewaySettings?.hostname)
                .setData(username.toString(), password.toString(), domain.toString())
                .setCancel {
                    callbackDialogResult = false
                    connectCancelledByUser = true
                    synchronized(dlgUserCredentialsLock) {
                        dlgUserCredentialsLock.notify()
                    }
                }
                .setDone { u, p, d ->
                    // 清空
                    username.setLength(0)
                    domain.setLength(0)
                    password.setLength(0)

                    // 读取用户操作后的凭据
                    username.append(u)
                    domain.append(p)
                    password.append(d)

                    p2vShowLoading("正在配置远程会话...")

                    callbackDialogResult = true
                    synchronized(dlgUserCredentialsLock) {
                        dlgUserCredentialsLock.notify()
                    }
                }.build().show(supportFragmentManager)
        }
        // 等待验证结果
        try {
            synchronized(dlgUserCredentialsLock) {
                dlgUserCredentialsLock.wait()
            }
        } catch (e: InterruptedException) {
        }

        return callbackDialogResult
    }

    override fun onVerifyCertificateEx(
        host: String,
        port: Long,
        commonName: String,
        subject: String,
        issuer: String,
        fingerprint: String,
        flags: Long
    ): Int {
        // 重置
        callbackDialogResult = false

        // 证书验证来自类型
        val type = when {
            flags and LibFreeRDP.VERIFY_CERT_FLAG_GATEWAY != 0L -> "RDP-Gateway"
            flags and LibFreeRDP.VERIFY_CERT_FLAG_REDIRECT != 0L -> "RDP-Redirect"
            else -> "RDP-Server $host:$port"
        }

        val finger = if (flags and LibFreeRDP.VERIFY_CERT_FLAG_FP_IS_PEM != 0L) {
            String.format("证书：\n%s", fingerprint)
        } else {
            String.format("指纹：\n%s", fingerprint)
        }

        val message =
            String.format("名称：%s\n主题：%s\n发行：%s", commonName, subject, issuer)

        // 证书验证
        ThreadUtils.runOnUiThread {
            p2vHideLoading()

            ScreenCertificateFragment.Builder()
                .setTitle(type)
                .setCertName(message)
                .setFinger(finger)
                .setCancel {
                    callbackDialogResult = false
                    connectCancelledByUser = true
                    synchronized(dlgVerifyCertificateLock) {
                        dlgVerifyCertificateLock.notify()
                    }
                }
                .setDone {
                    p2vShowLoading("正在验证登录凭证...")

                    callbackDialogResult = true
                    synchronized(dlgVerifyCertificateLock) {
                        dlgVerifyCertificateLock.notify()
                    }
                }.build().show(supportFragmentManager)
        }

        // 等待验证结果
        try {
            synchronized(dlgVerifyCertificateLock) {
                dlgVerifyCertificateLock.wait()
            }
        } catch (e: InterruptedException) {
        }

        return if (callbackDialogResult) 1 else 0
    }

    override fun onVerifyChangedCertificateEx(
        host: String,
        port: Long,
        commonName: String,
        subject: String,
        issuer: String,
        fingerprint: String,
        oldSubject: String,
        oldIssuer: String,
        oldFingerprint: String,
        flags: Long
    ): Int {
        // 重置
        callbackDialogResult = false

        // 证书验证来自类型
        val type = when {
            flags and LibFreeRDP.VERIFY_CERT_FLAG_GATEWAY != 0L -> "RDP-Gateway"
            flags and LibFreeRDP.VERIFY_CERT_FLAG_REDIRECT != 0L -> "RDP-Redirect"
            else -> "RDP-Server $host:$port"
        }

        val finger = if (flags and LibFreeRDP.VERIFY_CERT_FLAG_FP_IS_PEM != 0L) {
            String.format("证书：\n%s", fingerprint)
        } else {
            String.format("指纹：\n%s", fingerprint)
        }

        val message =
            String.format("证书变更名称：%s\n主题：%s\n发行：%s", commonName, subject, issuer)

        // 证书验证
        ThreadUtils.runOnUiThread {
            p2vHideLoading()

            ScreenCertificateFragment.Builder()
                .setTitle(type)
                .setCertName(message)
                .setFinger(finger)
                .setCancel {
                    callbackDialogResult = false
                    connectCancelledByUser = true
                    synchronized(dlgVerifyCertificateLock) {
                        dlgVerifyCertificateLock.notify()
                    }
                }
                .setDone {
                    p2vShowLoading()

                    callbackDialogResult = true
                    synchronized(dlgVerifyCertificateLock) {
                        dlgVerifyCertificateLock.notify()
                    }
                }.build().show(supportFragmentManager)
        }

        // 等待验证结果
        try {
            synchronized(dlgVerifyCertificateLock) {
                dlgVerifyCertificateLock.wait()
            }
        } catch (e: InterruptedException) {
        }

        return if (callbackDialogResult) 1 else 0
    }

    override fun onGraphicsUpdate(x: Int, y: Int, width: Int, height: Int) {
        presenter.v2pGraphicsUpdate(
            binding.rsvSession, rdpBitmap ?: return, x, y, width, height
        )
    }

    override fun onGraphicsResize(width: Int, height: Int, bpp: Int) {
        presenter.v2pGetSession { session ->
            // 设置位图
            rdpBitmap = if (bpp > 16) Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            else Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            // 设置画面
            session.surface = BitmapDrawable(resources, rdpBitmap)

            // UI线程更新画面
            ThreadUtils.runOnUiThread {
                binding.rsvSession.onSurfaceChange(session)
                binding.rsvSession.requestFocus()
                binding.rsvScroll.requestLayout()
            }
        }
    }

    override fun onSettingsChanged(width: Int, height: Int, bpp: Int) {
        presenter.v2pGetSession { session ->
            // 设置 Bitmap 色彩深度
            rdpBitmap = if (bpp > 16) Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            else Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            // 配置画面
            session.surface = BitmapDrawable(resources, rdpBitmap)

            // 判断远程自己是否自动调整了分辨率和色彩配置
            val settings = session.rdpConfig?.screenSettings ?: return@v2pGetSession
            when {
                settings.width != width && settings.width != width + 1 -> {
                    ToastUtils.showShort(String.format("远程主机不支持你配置的分辨率，已调整为 %sx%s", width, height))
                }
                settings.height != height -> {
                    ToastUtils.showShort(String.format("远程主机不支持你配置的分辨率，已调整为 %sx%s", width, height))
                }
                settings.colors != bpp -> {
                    ToastUtils.showShort(String.format("远程主机不支持你配置的色彩深度，已调整为 %s bit", bpp))
                }
            }
        }
    }

    /**
     * 本地复制文本回调
     */
    override fun onLocalClipboardChanged(data: String) {
        presenter.v2pSendClipboardData(data)
    }

    /**
     * 远程主机复制文本回调
     */
    override fun onRemoteClipboardChanged(data: String) {
        // 将远程复制内容放到本地的剪切板上
        clipboardManager.setLocalClipboardData(data)
    }

    /**
     * SessionView 回调 ============================================================================
     */
    override fun onSessionViewBeginTouch() {
        binding.rsvScroll.setScrollEnabled(false)
    }

    override fun onSessionViewEndTouch() {
        binding.rsvScroll.setScrollEnabled(true)
    }

    override fun onSessionViewLeftTouch(x: Int, y: Int, down: Boolean) {
        if (!down) presenter.v2pCancelDelayedMoveEvent()

        presenter.v2pSessionViewLeftTouch(x, y, down, toggleMouseButtons)

        if (!down) toggleMouseButtons = false
    }

    override fun onSessionViewRightTouch(x: Int, y: Int, down: Boolean) {
        if (!down) toggleMouseButtons = !toggleMouseButtons
    }

    override fun onSessionViewMove(x: Int, y: Int) {
        presenter.v2pSendDelayedMoveEvent(x, y)
    }

    override fun onSessionViewScroll(down: Boolean) {
        presenter.v2pSessionViewScroll(down)
    }

    /**
     * 外部连接设备相关
     */
    override fun onGenericMotionEvent(e: MotionEvent): Boolean {
        super.onGenericMotionEvent(e)
        when (e.action) {
            MotionEvent.ACTION_SCROLL -> {
                val vScroll = e.getAxisValue(MotionEvent.AXIS_VSCROLL)
                presenter.v2pGenericMotionScroll(vScroll)
            }
        }
        return true
    }

    /**
     * 软键盘打开回调
     */
    override fun vOnSoftKeyBoardShow() {
        if (connectSuccess) {
            binding.keyboardHeader.isVisible = true
        }
    }

    /**
     * 软键盘打开关闭
     */
    override fun vOnSoftKeyBoardClose() {
        if (!binding.keyboardSpecial.isVisible) {
            binding.keyboardHeader.isVisible = false
        }
    }

    /**
     * TouchPointerView 回调 ============================================================================
     */
    override fun onTouchPointerClose() {
        binding.tpvPointer.isInvisible = true
        binding.rsvSession.setTouchPointerPadding(0, 0)
    }

    override fun onTouchPointerLeftClick(x: Int, y: Int, down: Boolean) {
        presenter.v2pTouchPointerLeftClick(mapScreenPositionToSession(x, y), down)
    }

    override fun onTouchPointerRightClick(x: Int, y: Int, down: Boolean) {
        presenter.v2pTouchPointerRightClick(mapScreenPositionToSession(x, y), down)
    }

    override fun onTouchPointerMove(x: Int, y: Int) {
        presenter.v2pTouchPointerMove(mapScreenPositionToSession(x, y))
        // 边缘自动往中心滚动
        presenter.v2pAutoScrollPointer(binding.tpvPointer, binding.rsvSession, binding.rsvScroll)
    }

    override fun onTouchPointerScroll(down: Boolean) {
        presenter.v2pTouchPointerScroll(down)
    }

    override fun onTouchPointerToggleKeyboard() {
        if (binding.keyboardSpecial.isVisible) {
            binding.keyboardSpecial.isVisible = false
        }

        KeyboardUtils.toggleSoftInput()
    }

    override fun onTouchPointerToggleExtKeyboard() {
        KeyboardUtils.hideSoftInput(this)
        if (binding.keyboardSpecial.isVisible) {
            binding.keyboardSpecial.isVisible = false
            binding.keyboardHeader.isVisible = false
        } else {
            binding.keyboardSpecial.isVisible = true
            binding.keyboardHeader.isVisible = true
        }
    }

    override fun onTouchPointerResetScrollZoom() {
        binding.rsvSession.zoom = 1.0f
        binding.rsvScroll.scrollTo(0, 0)
    }

    private fun mapScreenPositionToSession(x: Int, y: Int): Point {
        var mappedX = ((x + binding.rsvScroll.scrollX).toFloat() / binding.rsvSession.zoom).toInt()
        var mappedY = ((y + binding.rsvScroll.scrollY).toFloat() / binding.rsvSession.zoom).toInt()
        rdpBitmap?.let {
            if (mappedX > it.width) mappedX = it.width
            if (mappedY > it.height) mappedY = it.height
        }
        return Point(mappedX, mappedY)
    }

    /**
     * Android 键盘输入处理
     *
     * 我们总是使用 unicode 值来处理来自 android 键盘的输入，除非按键修饰符（如 Win、Alt、Ctrl）被激活。
     *
     * 在这种情况下，我们将发送虚拟键代码以允许组合键（如 Win + E 打开资源管理器）。
     */
    override fun onKeyDown(keycode: Int, event: KeyEvent): Boolean {
        return rdpKeyboardMapper.processAndroidKeyEvent(event)
    }

    override fun onKeyUp(keycode: Int, event: KeyEvent): Boolean {
        return rdpKeyboardMapper.processAndroidKeyEvent(event)
    }

    /**
     * onKeyMultiple 用于输入一些特殊字符，如变音符号和一些符号字符
     */
    override fun onKeyMultiple(keyCode: Int, repeatCount: Int, event: KeyEvent?): Boolean {
        return rdpKeyboardMapper.processAndroidKeyEvent(event)
    }

    /**
     * KeyBoardView 回调 =================================================================================
     */
    override fun onKey(primaryCode: Int, keyCodes: IntArray) {
        rdpKeyboardMapper.processCustomKeyEvent(primaryCode)
    }


    /**
     * RdpKeyboardMapper 回调 ============================================================================
     */
    override fun processVirtualKey(virtualKeyCode: Int, down: Boolean) {
        presenter.v2pProcessVirtualKey(virtualKeyCode, down)
    }

    override fun processUnicodeKey(unicodeKey: Int) {
        presenter.v2pProcessUnicodeKey(unicodeKey)
    }

    override fun switchKeyboard(keyboardType: Int) {
        when (keyboardType) {
            RdpKeyboardMapper.KEYBOARD_TYPE_FUNCTIONKEYS ->
                binding.keyboardSpecial.keyboard = specialKeyboard
            RdpKeyboardMapper.KEYBOARD_TYPE_NUMPAD ->
                binding.keyboardSpecial.keyboard = numberPadKeyboard
            RdpKeyboardMapper.KEYBOARD_TYPE_CURSOR ->
                binding.keyboardSpecial.keyboard = cursorKeyboard
        }
    }

    override fun modifiersChanged() {
        // 检查键码列表中是否有任何键
        val keys: List<Keyboard.Key> = modifiersKeyboard.keys
        for (curKey in keys) {
            // 如果键是粘滞键 - 只需将其设置为关闭
            if (curKey.sticky) {
                when (rdpKeyboardMapper.getModifierState(curKey.codes[0])) {
                    RdpKeyboardMapper.KEYSTATE_ON -> {
                        curKey.on = true
                        curKey.pressed = false
                    }
                    RdpKeyboardMapper.KEYSTATE_OFF -> {
                        curKey.on = false
                        curKey.pressed = false
                    }
                    RdpKeyboardMapper.KEYSTATE_LOCKED -> {
                        curKey.on = true
                        curKey.pressed = true
                    }
                }
            }
        }

        // 刷新
        binding.keyboardHeader.invalidateAllKeys()
    }

    override fun p2vShowLoading() {
        loading.show(supportFragmentManager)
    }

    override fun p2vShowLoading(msg: String) {
        loading.arguments = loading.arguments?.also {
            val builder =
                it.getSerializable(NavigationKey.KEY_SERIALIZABLE) as? ScreenLoadingFragment.Builder
            builder?.loadingMessage = msg
            it.putSerializable(NavigationKey.KEY_SERIALIZABLE, builder)
        }
        loading.show(supportFragmentManager)
    }

    override fun p2vHideLoading() {
        loading.dismiss()
    }


    /**
     * 各种原因导致连接关闭
     */
    override fun vSessionClose(result: Int) {
        setResult(result, intent)
        presenter.v2pGetSession {
            LogUtils.e(LibFreeRDP.lastError(it.instance))
            ToastUtils.showShort(LibFreeRDP.lastError(it.instance))
        }
        finish()
    }


    override fun onBackPressed() {
        presenter.v2pGetSession(empty = {
            finish()
        }, callback = { session ->
            LibFreeRDP.disconnect(session.instance)
        })
    }

    override fun onDestroy() {
        unregisterReceiver(rdpEventReceiver)
        KeyboardUtils.unregisterSoftInputChangedListener(window)
        presenter.v2pFreeSession()
        super.onDestroy()
    }

    companion object {
        private const val KEY_RDP_ENTITY = "KEY_RDP_ENTITY"
        private const val KEY_RDP_INSTANCE = "KEY_RDP_INSTANCE"

        @JvmStatic
        fun openSelf(rdpEntity: RdpEntity) {
            ActivityUtils.startActivity(Intent(Utils.getApp(), ScreenActivity::class.java).also {
                it.putExtra(KEY_RDP_ENTITY, rdpEntity)
            })
        }
    }

}