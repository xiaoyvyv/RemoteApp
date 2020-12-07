package com.xiaoyv.ssh.terminal.presenter;

import com.trilead.ssh2.Session;
import com.xiaoyv.busines.base.BaseSubscriber;
import com.xiaoyv.busines.base.ImplBasePresenter;
import com.xiaoyv.busines.exception.RxException;
import com.xiaoyv.busines.room.entity.SshEntity;
import com.xiaoyv.ssh.terminal.contract.TerminalContract;
import com.xiaoyv.ssh.terminal.model.TerminalModel;

import java.io.IOException;

/**
 * TerminalPresenter
 *
 * @author why
 * @since 2020/12/06
 **/
public class TerminalPresenter extends ImplBasePresenter<TerminalContract.View> implements TerminalContract.Presenter {
    private TerminalContract.Model model;

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
                    public void onError(RxException e) {
                        getView().p2vShowToast(e.getMessage());
                    }

                    @Override
                    public void onSuccess(Session session) {
                        getView().p2vConnectSuccess(session);
                    }
                });
    }
}
