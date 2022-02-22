package com.xiaoyv.ssh.terminal;

import androidx.annotation.NonNull;

import com.romide.terminal.emulatorview.compat.KeycodeConstants;
import com.trilead.ssh2.Session;
import com.xiaoyv.blueprint.base.ImplBasePresenter;
import com.xiaoyv.blueprint.base.rxjava.BaseSubscriber;
import com.xiaoyv.blueprint.exception.RxException;
import com.xiaoyv.busines.bean.ssh.KeyCodeBean;
import com.xiaoyv.busines.room.entity.SshEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * TerminalPresenter
 *
 * @author why
 * @since 2020/12/06
 **/
public class TerminalPresenter extends ImplBasePresenter<TerminalContract.View> implements TerminalContract.Presenter {
    private final TerminalContract.Model model;

    public TerminalPresenter() {
        this.model = new TerminalModel();
    }

    @Override
    public void v2pConnectSsh(SshEntity sshEntity) {
        model.p2mConnectSsh(sshEntity)
                .compose(bindTransformer())
                .to(bindLifecycle())
                .subscribe(new BaseSubscriber<Session>() {
                    @Override
                    public void onError(@NonNull RxException e) {
                        getView().p2vShowToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(Session session) {
                        getView().p2vConnectSuccess(session);
                    }
                });
    }

    @Override
    public List<KeyCodeBean> v2pGetSymbol() {
        List<KeyCodeBean> keyCodeBeans = new ArrayList<>();
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_ESCAPE, "Esc"));
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_TAB, "Tab"));
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_PAGE_UP, "PgUp"));
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_MOVE_HOME, "Home"));
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_DPAD_UP, "↑"));
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_MOVE_END, "End"));
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_FUNCTION, "Fn"));
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_CTRL_LEFT, "Ctrl"));
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_ALT_LEFT, "Alt"));
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_PAGE_DOWN, "PgDn"));
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_DPAD_LEFT, "←"));
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_DPAD_DOWN, "↓"));
        keyCodeBeans.add(new KeyCodeBean(KeycodeConstants.KEYCODE_DPAD_RIGHT, "→"));
        keyCodeBeans.add(new KeyCodeBean(0, "↔"));
        return keyCodeBeans;
    }

    @Override
    public void v2pOnDestroy() {
        model.p2mClose();
    }
}
