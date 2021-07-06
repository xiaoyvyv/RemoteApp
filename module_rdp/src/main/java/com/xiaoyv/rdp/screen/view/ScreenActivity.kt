package com.xiaoyv.rdp.screen.view

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import com.blankj.utilcode.util.*
import com.freerdp.freerdpcore.application.RdpApp
import com.freerdp.freerdpcore.domain.RdpConfig
import com.freerdp.freerdpcore.domain.RdpSession
import com.freerdp.freerdpcore.services.LibFreeRDP
import com.freerdp.freerdpcore.services.UiEventListener
import com.freerdp.freerdpcore.utils.Mouse
import com.freerdp.freerdpcore.view.RdpSessionView
import com.xiaoyv.busines.base.BaseMvpActivity
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.rdp.R
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
    FreeScrollView.ScrollView2DListener {
    private lateinit var binding: RdpActivityScreenBinding
    private val dlgVerifyCertificateLock = Object()
    private val dlgUserCredentialsLock = Object()
    private var rdpEntity: RdpEntity? = null
    private var rdpConfig: RdpConfig? = null

    private var callbackDialogResult = false
    private var connectCancelledByUser = false
    private var toggleMouseButtons = false

    private var rdpUri: Uri? = null
    private var rdpInstance: Long = -1


    private var bitmap: Bitmap? = null

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
        }
        /**
         * 连接异常
         */
        onConnectionFailure = {
            presenter.v2pCancelDelayedMoveEvent()
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
            vSessionClose(RESULT_OK)
        }
    }

    override fun createPresenter(): ScreenPresenter {
        return ScreenPresenter()
    }

    override fun createContentView(): View {
//        ScreenUtils.setLandscape(this)
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

    }


    override fun initData() {
        // 注册 Rdp 事件广播接收器
        registerReceiver(rdpEventReceiver, IntentFilter(RdpApp.ACTION_EVENT_FREERDP))

        vProcessArgument()
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
    }

    override fun onScrollChanged(
        scrollView: FreeScrollView, x: Int, y: Int, oldx: Int, oldy: Int
    ) {

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

        LogUtils.e("Rdp结果：" + callbackDialogResult)
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
        LogUtils.e("onVerifyChangedCertificate")
        ToastUtils.showShort("onVerifyChangedCertificate")
        return 0
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
            // 设置画面位图
            bitmap = if (bpp > 16) Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            else Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

            // 配置画面
            session.surface = BitmapDrawable(resources, bitmap)

            // 检测PC是否支持我们设置的分辨率，不一样则弹出提示自动调整的分辨率
            val settings = session.rdpConfig?.screenSettings ?: return@v2pGetSession
            if (settings.width != width && settings.width != width + 1
                || settings.height != height
                || settings.colors != bpp
            ) {
                ToastUtils.showShort(R.string.info_capabilities_changed)
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