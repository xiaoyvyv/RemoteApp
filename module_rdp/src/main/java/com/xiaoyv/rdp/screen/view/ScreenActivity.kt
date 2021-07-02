package com.xiaoyv.rdp.screen.view

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.ScaleGestureDetector
import android.view.View
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.Utils
import com.freerdp.freerdpcore.application.RdpApp
import com.freerdp.freerdpcore.services.LibFreeRDP
import com.freerdp.freerdpcore.view.RdpSessionView
import com.xiaoyv.busines.base.BaseMvpActivity
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
class ScreenActivity : BaseMvpActivity<ScreenContract.View?, ScreenPresenter?>(),
    ScreenContract.View, LibFreeRDP.UiEventListener, RdpSessionView.SessionViewListener,
    FreeScrollView.ScrollView2DListener {
    private lateinit var binding: RdpActivityScreenBinding

    private var rdpEntity: RdpEntity? = null

    /**
     * Rdp 事件监听
     */
    private val rdpEventReceiver = LibFreeRDPBroadcastReceiver().apply {
        onPrepareConnect = {

        }
        onConnectionSuccess = {
        }
        onConnectionSuccess = {

        }
        onDisconnecting = {

        }
        onDisconnected = {

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
        rdpEntity = (intent.getSerializableExtra(KEY_RDP_ENTITY) as? RdpEntity) ?: rdpEntity

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


    }


    override fun onScrollChanged(
        scrollView: FreeScrollView?,
        x: Int,
        y: Int,
        oldx: Int,
        oldy: Int
    ) {

    }


    /**
     * UiEventListener 回调 ========================================================================
     */
    override fun onSettingsChanged(width: Int, height: Int, bpp: Int) {}

    override fun onAuthenticate(
        username: StringBuilder,
        domain: StringBuilder,
        password: StringBuilder
    ): Boolean {
        return false
    }

    override fun onGatewayAuthenticate(
        username: StringBuilder,
        domain: StringBuilder,
        password: StringBuilder
    ): Boolean {
        return false
    }

    override fun onVerifiyCertificate(
        commonName: String,
        subject: String,
        issuer: String,
        fingerprint: String,
        mismatch: Boolean
    ): Int {
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
        return 0
    }

    override fun onGraphicsUpdate(x: Int, y: Int, width: Int, height: Int) {}
    override fun onGraphicsResize(width: Int, height: Int, bpp: Int) {}
    override fun onRemoteClipboardChanged(data: String) {}

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

        @JvmStatic
        fun openSelf(rdpEntity: RdpEntity) {
            ActivityUtils.startActivity(Intent(Utils.getApp(), ScreenActivity::class.java).also {
                it.putExtra(KEY_RDP_ENTITY, rdpEntity)
            })
        }
    }
}