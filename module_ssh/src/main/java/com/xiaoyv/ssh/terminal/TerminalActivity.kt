package com.xiaoyv.ssh.terminal

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.Utils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.romide.terminal.emulatorview.ColorScheme
import com.romide.terminal.emulatorview.EmulatorView
import com.romide.terminal.emulatorview.TermSession
import com.romide.terminal.emulatorview.compat.KeycodeConstants
import com.trilead.ssh2.Session
import com.xiaoyv.blueprint.base.binding.BaseMvpBindingActivity
import com.xiaoyv.blueprint.base.rxjava.event.RxEvent
import com.xiaoyv.business.bean.ssh.KeyCodeBean
import com.xiaoyv.business.config.NavigationKey
import com.xiaoyv.business.room.entity.SshEntity
import com.xiaoyv.business.rx.RxEventTag
import com.xiaoyv.desktop.ssh.R
import com.xiaoyv.desktop.ssh.databinding.SshActivityTerminalBinding
import com.xiaoyv.ssh.main.view.EmulatorViewGestureListener
import com.xiaoyv.ssh.sftp.SftpActivity
import com.xiaoyv.widget.utils.doOnBarClick
import java.util.*

/**
 * TerminalActivity
 *
 * @author why
 * @since 2020/12/06
 */
class TerminalActivity :
    BaseMvpBindingActivity<SshActivityTerminalBinding, TerminalContract.View, TerminalPresenter>(),
    TerminalContract.View {

    private lateinit var terminalBinder: TerminalBinder
    private lateinit var typeAdapter: BaseBinderAdapter
    private lateinit var colorScheme: ColorScheme

    private var emulatorView: EmulatorView? = null
    private var sshEntity: SshEntity = SshEntity()

    override fun createPresenter() = TerminalPresenter()

    override fun createContentBinding(layoutInflater: LayoutInflater): SshActivityTerminalBinding {
        return SshActivityTerminalBinding.inflate(layoutInflater)
    }

    override fun initIntentData(intent: Intent, bundle: Bundle, isNewIntent: Boolean) {
        sshEntity = intent.getSerializableExtra(NavigationKey.KEY_SERIALIZABLE) as? SshEntity
            ?: sshEntity

        if (sshEntity.id == 0L) {
            onBackPressed()
        }
    }

    override fun fix5497() = true

    override fun initView() {
        binding.toolbar.title = String.format("%s@%s", sshEntity.account, sshEntity.ip)

        binding.toolbar.setRightIcon(
            R.drawable.business_icon_file_dir,
            onBarClickListener = doOnBarClick { view, which ->
                SftpActivity.openSelf(sshEntity)
            })
    }

    override fun initData() {
        colorScheme = ColorScheme(
            ColorUtils.getColor(R.color.ui_text_c4),
            ColorUtils.getColor(R.color.ui_black),
            ColorUtils.getColor(R.color.ui_black),
            ColorUtils.getColor(R.color.ui_text_c4)
        )
        binding.evTerminal.setBackgroundColor(colorScheme.backColor)
        binding.vHr.setBackgroundColor(colorScheme.foreColor)

        terminalBinder = TerminalBinder()
        typeAdapter = BaseBinderAdapter()
        typeAdapter.addItemBinder(KeyCodeBean::class.java, terminalBinder)
        binding.rvKeyboard.adapter = typeAdapter

        typeAdapter.setList(presenter.v2pGetSymbol())
        typeAdapter.notifyItemRangeChanged(0, typeAdapter.itemCount)

        addReceiveEventTag(RxEventTag.EVENT_SSH_DISCONNECT)
    }

    override fun onPresenterCreated() {
        presenter.v2pConnectSsh(sshEntity)
    }

    override fun onResume() {
        super.onResume()
        emulatorView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        emulatorView?.onPause()
    }

    override fun onReceiveRxEvent(rxEvent: RxEvent, rxEventTag: String) {
        if (rxEventTag == RxEventTag.EVENT_SSH_DISCONNECT) {
            p2vShowToast("SSH 已经断开")

            onBackPressed()
        }
    }

    override fun p2vConnectFail(errMsg: String) {
        p2vShowToast(errMsg)

        onBackPressed()
    }

    override fun p2vConnectSuccess(session: Session) {
        val stdout = session.stdout
        val stdin = session.stdin
        val termSession: TermSession = object : TermSession() {
            override fun onProcessExit() {
                super.onProcessExit()
                onBackPressed()
            }
        }
        termSession.termIn = stdout
        termSession.termOut = stdin
        termSession.setColorScheme(colorScheme)
        termSession.setDefaultUTF8Mode(true)
        termSession.title = String.format(
            Locale.getDefault(),
            "%s：%s",
            sshEntity.account,
            sshEntity.ip
        )
        termSession.setUpdateCallback {

        }

        emulatorView = EmulatorView(this, termSession, resources.displayMetrics).apply {
            this.setTextSize(12)
            this.setColorScheme(colorScheme)
            this.updateSize(false)
            this.setUseCookedIME(true)
            this.setBackKeyCharacter(0)
            this.setAltSendsEsc(false)
            this.setControlKeyCode(KeycodeConstants.KEYCODE_CTRL_LEFT)
            this.setFnKeyCode(KeycodeConstants.KEYCODE_FUNCTION)
            this.setMouseTracking(true)
            this.setTermType("xterm")
            this.setCursorBlink(0)
            this.setCursorBlinkPeriod(250)
            this.setExtGestureListener(EmulatorViewGestureListener(this))
            this.onResume()

            binding.evTerminal.addView(this)
            terminalBinder.bindTerminal(this)
        }

        registerForContextMenu(binding.evTerminal)
    }

    override fun onDestroy() {
        runCatching {
            emulatorView?.termSession?.finish()
        }
        super.onDestroy()
    }

    companion object {

        fun openSelf(sshEntity: SshEntity) {
            val starter = Intent(Utils.getApp(), TerminalActivity::class.java)
            starter.putExtra(NavigationKey.KEY_SERIALIZABLE, sshEntity)
            ActivityUtils.startActivity(starter)
        }
    }
}