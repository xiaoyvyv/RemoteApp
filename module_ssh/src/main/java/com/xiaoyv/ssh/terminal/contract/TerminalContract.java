package com.xiaoyv.ssh.terminal.contract;

import com.trilead.ssh2.Session;
import com.xiaoyv.busines.base.IBaseModel;
import com.xiaoyv.busines.base.IBasePresenter;
import com.xiaoyv.busines.base.IBaseView;
import com.xiaoyv.busines.room.entity.SshEntity;

import java.io.IOException;

import io.reactivex.rxjava3.core.Observable;

/**
 * TerminalContract
 *
 * @author why
 * @since 2020/12/06
 **/
public interface TerminalContract {

    interface View extends IBaseView {
        /**
         * 连接成功
         * @param session session
         */
        void p2vConnectSuccess(Session session)  ;
    }

    interface Presenter extends IBasePresenter {

        /**
         * 连接SSH
         *
         * @param sshEntity ssh
         */
        void v2pConnectSsh(SshEntity sshEntity);
    }

    interface Model extends IBaseModel {
        /**
         * 连接SSH
         *
         * @param sshEntity SSH信息
         * @return 观察者
         */
        Observable<Session> p2mConnectSsh(SshEntity sshEntity);
    }
}
