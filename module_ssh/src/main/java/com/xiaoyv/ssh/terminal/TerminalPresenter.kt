package com.xiaoyv.ssh.terminal

import com.blankj.utilcode.util.LogUtils
import com.romide.terminal.emulatorview.TermSession
import com.romide.terminal.emulatorview.compat.KeycodeConstants
import com.xiaoyv.blueprint.base.ImplBasePresenter
import com.xiaoyv.blueprint.base.subscribesWithPresenter
import com.xiaoyv.busines.bean.ssh.KeyCodeBean
import com.xiaoyv.busines.room.entity.SshEntity

/**
 * TerminalPresenter
 *
 * @author why
 * @since 2020/12/06
 */
class TerminalPresenter : ImplBasePresenter<TerminalContract.View>(), TerminalContract.Presenter {
    private val model = TerminalModel()

    override fun v2pConnectSsh(sshEntity: SshEntity) {
        model.p2mConnectSsh(sshEntity)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    requireView.p2vConnectSuccess(it)
                },
                onError = {
                    requireView.p2vConnectFail(it.message.orEmpty())
                }
            )
    }

    override fun v2pGetSymbol() = arrayListOf<KeyCodeBean>().apply {
        add(KeyCodeBean(KeycodeConstants.KEYCODE_ESCAPE, "Esc"))
        add(KeyCodeBean(KeycodeConstants.KEYCODE_TAB, "Tab"))
        add(KeyCodeBean(KeycodeConstants.KEYCODE_PAGE_UP, "PgUp"))
        add(KeyCodeBean(KeycodeConstants.KEYCODE_MOVE_HOME, "Home"))
        add(KeyCodeBean(KeycodeConstants.KEYCODE_DPAD_UP, "↑"))
        add(KeyCodeBean(KeycodeConstants.KEYCODE_MOVE_END, "End"))
        add(KeyCodeBean(KeycodeConstants.KEYCODE_FUNCTION, "Fn"))
        add(KeyCodeBean(KeycodeConstants.KEYCODE_CTRL_LEFT, "Ctrl"))
        add(KeyCodeBean(KeycodeConstants.KEYCODE_ALT_LEFT, "Alt"))
        add(KeyCodeBean(KeycodeConstants.KEYCODE_PAGE_DOWN, "PgDn"))
        add(KeyCodeBean(KeycodeConstants.KEYCODE_DPAD_LEFT, "←"))
        add(KeyCodeBean(KeycodeConstants.KEYCODE_DPAD_DOWN, "↓"))
        add(KeyCodeBean(KeycodeConstants.KEYCODE_DPAD_RIGHT, "→"))
        add(KeyCodeBean(0, "↔"))
    }

    override fun v2pReleaseSession(termSession: TermSession) {
        model.p2mReleaseSession(termSession)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    getView().p2vReleaseSuccess(true)
                },
                onError = {
                    getView().p2vReleaseSuccess(false)
                }
            )
    }

    override fun v2pDoCommandLs(dirName: String) {
        model.p2mDoCommandLs(dirName)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    LogUtils.json(it)
                },
                onError = {
                    LogUtils.e(it.message)
                }
            )
    }
}