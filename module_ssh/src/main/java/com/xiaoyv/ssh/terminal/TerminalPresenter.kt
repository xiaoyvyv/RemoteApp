package com.xiaoyv.ssh.terminal

import com.blankj.utilcode.util.LogUtils
import com.romide.terminal.emulatorview.TermSession
import com.romide.terminal.emulatorview.compat.KeycodeConstants
import com.xiaoyv.blueprint.base.ImplBasePresenter
import com.xiaoyv.blueprint.base.subscribesWithPresenter
import com.xiaoyv.business.bean.ssh.KeyCodeBean
import com.xiaoyv.business.room.entity.SshEntity
import io.reactivex.rxjava3.core.Observable
import java.util.concurrent.TimeUnit

/**
 * TerminalPresenter
 *
 * @author why
 * @since 2020/12/06
 */
class TerminalPresenter : ImplBasePresenter<TerminalContract.View>(), TerminalContract.Presenter {
    private val model = TerminalModel()

    override fun v2pConnectSsh(sshEntity: SshEntity) {
        requireView.p2vShowLoading("正在连接 SSH")

        model.p2mConnectSsh(sshEntity)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    requireView.p2vHideLoading()

                    // 开启心跳
                    pStartHeartbeat()

                    requireView.p2vConnectSuccess(it)
                },
                onError = {
                    requireView.p2vHideLoading()

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

    }

    override fun v2pOnDestroy() {
        model.p2mReleaseSession()
    }

    override fun pStartHeartbeat() {
        Observable.interval(10, TimeUnit.SECONDS)
            .flatMap {
                Observable.create<Boolean> {
                    model.p2mSendHeartbeatPackets()
                    it.onNext(true)
                }
            }
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    LogUtils.i("TERMINAL SSH 心跳包")
                },
                onError = {
                    LogUtils.e("TERMINAL SSH 心跳结束")
                }
            )
    }
}