package com.xiaoyv.rdp.screen.view

import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Message
import android.view.ScaleGestureDetector
import android.view.View
import com.blankj.utilcode.util.*
import com.freerdp.freerdpcore.application.RdpApp
import com.freerdp.freerdpcore.domain.RdpConfig
import com.freerdp.freerdpcore.domain.RdpSession
import com.freerdp.freerdpcore.presentation.SessionActivity
import com.freerdp.freerdpcore.services.LibFreeRDP
import com.freerdp.freerdpcore.view.RdpSessionView
import com.xiaoyv.busines.base.BaseMvpActivity
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.rdp.R
import com.xiaoyv.rdp.databinding.RdpActivityScreenBinding
import com.xiaoyv.rdp.databinding.RdpActivityScreenCredentialsBinding
import com.xiaoyv.rdp.screen.config.LibFreeRDPBroadcastReceiver
import com.xiaoyv.rdp.screen.config.PinchZoomListener
import com.xiaoyv.rdp.screen.contract.ScreenContract
import com.xiaoyv.rdp.screen.presenter.ScreenPresenter
import com.xiaoyv.ui.dialog.normal.NormalDialog
import com.xiaoyv.ui.scroll.FreeScrollView

/**
 * ScreenView
 *
 * @author why
 * @since 2020/12/02
 */
class ScreenActivity : BaseMvpActivity<ScreenContract.View, ScreenPresenter>(),
    ScreenContract.View, LibFreeRDP.UiEventListener, RdpSessionView.SessionViewListener,
    FreeScrollView.ScrollView2DListener {
    private lateinit var binding: RdpActivityScreenBinding
    private lateinit var userBinding: RdpActivityScreenCredentialsBinding
    private lateinit var dlgVerifyCertificate: NormalDialog
    private lateinit var dlgUserCredentials: NormalDialog
    private val dlgVerifyCertificateLock = Object()
    private val dlgUserCredentialsLock = Object()
    private var callbackDialogResult = false
    private var connectCancelledByUser = false
    private var rdpEntity: RdpEntity? = null

    private var rdpUri: Uri? = null
    private var rdpInstance: Long = -1

    private var bitmap: Bitmap? = null

    /**
     * Rdp 事件监听
     */
    private val rdpEventReceiver = LibFreeRDPBroadcastReceiver().apply {
        onPrepareConnect = {
            LogUtils.e("准备连接")

        }
        onConnectionSuccess = {
            LogUtils.e("连接成功")
            // 连接成功，开始绑定画面
            presenter.v2pGetSession { session ->
                p2vBindSession(session)
            }
        }
        onConnectionFailure = {
            LogUtils.e("连接失败")

        }
        onDisconnecting = {
            LogUtils.e("连接断开中")

        }
        onDisconnected = {
            LogUtils.e("连接已经断开")
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

        // 验证相关对话框
        vCreateDialog()
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
                val rdpConfig = RdpConfig()
                presenter.v2pConnectWithConfig(rdpConfig)
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
        binding.rsvScroll.requestLayout()
//        rdpKeyboardMapper.reset(this)
    }

    override fun vSessionClose(result: Int) {

    }


    override fun vCreateDialog() {
        // 构建验证证书对话框
        dlgVerifyCertificate = NormalDialog(this).apply {
            title = StringUtils.getString(R.string.dlg_title_verify_certificate)
            cancelText = StringUtils.getString(R.string.ui_common_no)
            doneText = StringUtils.getString(R.string.ui_common_yes)
            cancelClickListener = {
                callbackDialogResult = false
                connectCancelledByUser = true
                synchronized(dlgVerifyCertificateLock) {
                    dlgVerifyCertificateLock.notify()
                }
                true
            }
            doneClickListener = {
                callbackDialogResult = true
                synchronized(dlgVerifyCertificateLock) {
                    dlgVerifyCertificateLock.notify()
                }
                true
            }
            setCancelable(false)
        }

        // 构建用户凭证验证对话框
        userBinding = RdpActivityScreenCredentialsBinding.inflate(layoutInflater)
        dlgUserCredentials = NormalDialog(this).apply {
            title = StringUtils.getString(R.string.dlg_title_credentials)
            customView = userBinding.root
            cancelText = StringUtils.getString(R.string.ui_common_no)
            doneText = StringUtils.getString(R.string.ui_common_yes)
            cancelClickListener = {
                callbackDialogResult = false
                connectCancelledByUser = true
                synchronized(dlgUserCredentialsLock) {
                    dlgUserCredentialsLock.notify()
                }
                true
            }
            doneClickListener = {
                callbackDialogResult = true
                synchronized(dlgUserCredentialsLock) {
                    dlgUserCredentialsLock.notify()
                }
                true
            }
            setCancelable(false)
        }
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
            // 设置值
            userBinding.editTextUsername.setText(username)
            userBinding.editTextDomain.setText(domain)
            userBinding.editTextPassword.setText(password)
            // 显示
            dlgUserCredentials.title = StringUtils.getString(R.string.dlg_title_credentials)
            dlgUserCredentials.show()
        }

        // 等待验证结果
        try {
            synchronized(dlgUserCredentialsLock) {
                dlgUserCredentialsLock.wait()
            }
        } catch (e: InterruptedException) {
        }

        // 清空
        username.setLength(0)
        domain.setLength(0)
        password.setLength(0)

        // 读取用户操作后的凭据
        username.append(userBinding.editTextUsername.text.toString())
        domain.append(userBinding.editTextDomain.text.toString())
        password.append(userBinding.editTextPassword.text.toString())

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
            // 设置值
            userBinding.editTextUsername.setText(username)
            userBinding.editTextDomain.setText(domain)
            userBinding.editTextPassword.setText(password)
            // 显示
            dlgUserCredentials.title = StringUtils.getString(R.string.dlg_title_credentials_gateway)
            dlgUserCredentials.show()
        }

        // 等待验证结果
        try {
            synchronized(dlgUserCredentialsLock) {
                dlgUserCredentialsLock.wait()
            }
        } catch (e: InterruptedException) {
        }

        // 清空
        username.setLength(0)
        domain.setLength(0)
        password.setLength(0)

        // 读取用户操作后的凭据
        username.append(userBinding.editTextUsername.text.toString())
        domain.append(userBinding.editTextDomain.text.toString())
        password.append(userBinding.editTextPassword.text.toString())

        return callbackDialogResult
    }

    override fun onVerifiyCertificate(
        commonName: String, subject: String, issuer: String, fingerprint: String, mismatch: Boolean
    ): Int {
        ToastUtils.showShort("onVerifiyCertificate")

        return 0
    }

    override fun onVerifyChangedCertificate(
        commonName: String,
        subject: String,
        issuer: String,
        fingerprint: String,
        oldSubject: String,
        oldIssuer: String,
        oldFingerprint: String
    ): Int {
        ToastUtils.showShort("onVerifyChangedCertificate")
        return 0
    }

    override fun onGraphicsUpdate(x: Int, y: Int, width: Int, height: Int) {
        presenter.v2pGetSession { session ->
            LibFreeRDP.updateGraphics(session.instance, bitmap, x, y, width, height)

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

    }

    override fun onSessionViewEndTouch() {

    }

    override fun onSessionViewLeftTouch(x: Int, y: Int, down: Boolean) {

    }

    override fun onSessionViewRightTouch(x: Int, y: Int, down: Boolean) {

    }

    override fun onSessionViewMove(x: Int, y: Int) {

    }

    override fun onSessionViewScroll(down: Boolean) {

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