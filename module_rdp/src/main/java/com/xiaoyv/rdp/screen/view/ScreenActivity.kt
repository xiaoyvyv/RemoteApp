package com.xiaoyv.rdp.screen.view

import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.blankj.utilcode.util.*
import com.freerdp.freerdpcore.application.RdpApp
import com.freerdp.freerdpcore.domain.RdpConfig
import com.freerdp.freerdpcore.domain.RdpSession
import com.freerdp.freerdpcore.services.LibFreeRDP
import com.freerdp.freerdpcore.services.UiEventListener
import com.freerdp.freerdpcore.utils.Mouse
import com.freerdp.freerdpcore.view.RdpPointerView
import com.freerdp.freerdpcore.view.RdpSessionView
import com.xiaoyv.busines.base.BaseMvpActivity
import com.xiaoyv.busines.config.NavigationKey
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.rdp.databinding.RdpActivityScreenBinding
import com.xiaoyv.rdp.screen.config.LibFreeRDPBroadcastReceiver
import com.xiaoyv.rdp.screen.config.PinchZoomListener
import com.xiaoyv.rdp.screen.contract.ScreenContract
import com.xiaoyv.rdp.screen.presenter.ScreenPresenter
import com.xiaoyv.ui.scroll.FreeScrollView


/**
 * ScreenView
 *
 * @author why
 * @since 2020/12/02
 */
class ScreenActivity : BaseMvpActivity<ScreenContract.View, ScreenPresenter>(),
    ScreenContract.View, UiEventListener, RdpSessionView.SessionViewListener,
    FreeScrollView.ScrollView2DListener, RdpPointerView.TouchPointerListener {
    private lateinit var binding: RdpActivityScreenBinding
    private val dlgVerifyCertificateLock = Object()
    private val dlgUserCredentialsLock = Object()
    private var rdpEntity: RdpEntity? = null
    private var rdpConfig: RdpConfig? = null
    private var callbackDialogResult = false

    private var connectCancelledByUser = false
    private var toggleMouseButtons = false
    private var screenLandscape = false
    private var rdpUri: Uri? = null

    private var rdpInstance: Long = -1
    private var bitmap: Bitmap? = null

    private lateinit var loading: ScreenLoadingFragment

    /**
     * Rdp 事件监听
     */
    private val rdpEventReceiver = LibFreeRDPBroadcastReceiver().apply {
        /**
         * 连接成功
         */
        onConnectionSuccess = {
            presenter.v2pGetSession { session ->
                p2vBindSession(session)
            }
            p2vHideLoading()
        }
        /**
         * 连接异常
         */
        onConnectionFailure = {
            presenter.v2pCancelDelayedMoveEvent()

            p2vHideLoading()
            vSessionClose(RESULT_CANCELED)
        }
        /**
         * 连接正常退出
         */
        onDisconnected = {
            presenter.v2pCancelDelayedMoveEvent()
            presenter.v2pGetSession { session ->
                session.uiEventListener = null
            }

            p2vHideLoading()
            vSessionClose(RESULT_OK)
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
            ScaleGestureDetector(this, PinchZoomListener(binding.rsvScroll, binding.rsvSession))
        )
        binding.rsvSession.setSessionViewListener(this)
        binding.rsvSession.requestFocus()

        binding.rsvScroll.setScrollViewListener(this)
        binding.tpvPointer.setTouchPointerListener(this)
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

    }

    /**
     * P层初始化完成，在这使用Presenter进行数据请求
     */
    override fun onPresenterCreated() {
        binding.root.post {
            // 在 UI 绘制完才开始准备连接，否则屏幕宽度高度信息不准确
            vProcessArgument()

            // 加载中对话框
            loading = ScreenLoadingFragment.Builder()
                .setTitle("远程桌面连接")
                .setLoadingTitle(String.format("正在连接到：\n%s",rdpConfig?.hostname))
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
                rdpConfig = RdpConfig().also {
                    presenter.v2pConnectWithConfig(it)
                }
            }
            else -> {
                // 其他则结束对话
                vSessionClose(RESULT_OK)
            }
        }
    }

    override fun p2vScreenLandscape() = screenLandscape

    override fun p2vStartConnect(rdpSession: RdpSession): RdpSession {
        rdpSession.uiEventListener = this
        // 配置广播当前 session
        rdpEventReceiver.currentInstance = rdpSession.instance
        return rdpSession
    }

    override fun p2vBindSession(rdpSession: RdpSession) {
        binding.rsvSession.onSurfaceChange(rdpSession)
        binding.rsvSession.requestFocus()
        binding.rsvScroll.requestLayout()
//        rdpKeyboardMapper.reset(this)

        hideSystemUi()
    }

    override fun onScrollChanged(
        scrollView: FreeScrollView, x: Int, y: Int, oldx: Int, oldy: Int
    ) {

    }


    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
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
        presenter.v2pGetSession { session ->
            LibFreeRDP.updateGraphics(session.instance, bitmap!!, x, y, width, height)

            binding.rsvSession.addInvalidRegion(Rect(x, y, x + width, y + height))

            // 刷新 SessionView
            ThreadUtils.runOnUiThread {
                binding.rsvSession.invalidateRegion()
            }
        }
    }

    override fun onGraphicsResize(width: Int, height: Int, bpp: Int) {
        presenter.v2pGetSession { session ->
            // 设置位图
            bitmap = if (bpp > 16) Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            else Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            // 设置画面
            session.surface = BitmapDrawable(resources, bitmap)

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
            bitmap = if (bpp > 16) Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            else Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            // 配置画面
            session.surface = BitmapDrawable(resources, bitmap)

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


    override fun onRemoteClipboardChanged(data: String) {

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

        presenter.v2pGetSession { session ->
            LibFreeRDP.sendCursorEvent(
                session.instance, x, y,
                if (toggleMouseButtons) Mouse.getRightButtonEvent(this, down)
                else Mouse.getLeftButtonEvent(this, down)
            )
        }

        if (!down) toggleMouseButtons = false
    }


    override fun onSessionViewRightTouch(x: Int, y: Int, down: Boolean) {
        if (!down) toggleMouseButtons = !toggleMouseButtons
    }

    override fun onSessionViewMove(x: Int, y: Int) {
        presenter.v2pSendDelayedMoveEvent(x, y)
    }

    override fun onSessionViewScroll(down: Boolean) {
        presenter.v2pGetSession { session ->
            LibFreeRDP.sendCursorEvent(session.instance, 0, 0, Mouse.getScrollEvent(this, down))
        }
    }

    override fun onGenericMotionEvent(e: MotionEvent): Boolean {
        super.onGenericMotionEvent(e)
        when (e.action) {
            MotionEvent.ACTION_SCROLL -> {
                val vScroll = e.getAxisValue(MotionEvent.AXIS_VSCROLL)
                presenter.v2pGetSession { session ->
                    if (vScroll < 0) {
                        LibFreeRDP.sendCursorEvent(
                            session.instance, 0, 0,
                            Mouse.getScrollEvent(this, false)
                        )
                    }
                    if (vScroll > 0) {
                        LibFreeRDP.sendCursorEvent(
                            session.instance, 0, 0,
                            Mouse.getScrollEvent(this, true)
                        )
                    }
                }
            }
        }
        return true
    }

    /**
     * TouchPointerView 回调 ============================================================================
     */
    override fun onTouchPointerClose() {

    }

    override fun onTouchPointerLeftClick(x: Int, y: Int, down: Boolean) {

    }

    override fun onTouchPointerRightClick(x: Int, y: Int, down: Boolean) {

    }

    override fun onTouchPointerMove(x: Int, y: Int) {

    }

    override fun onTouchPointerScroll(down: Boolean) {

    }

    override fun onTouchPointerToggleKeyboard() {

    }

    override fun onTouchPointerToggleExtKeyboard() {

    }

    override fun onTouchPointerResetScrollZoom() {

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