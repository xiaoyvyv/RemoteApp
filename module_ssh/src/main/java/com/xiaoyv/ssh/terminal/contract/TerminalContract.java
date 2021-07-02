package com.xiaoyv.ssh.terminal.contract;

import com.trilead.ssh2.Session;
import com.xiaoyv.busines.base.IBaseModel;
import com.xiaoyv.busines.base.IBasePresenter;
import com.xiaoyv.busines.base.IBaseView;
import com.xiaoyv.busines.bean.ssh.KeyCodeBean;
import com.xiaoyv.busines.room.entity.SshEntity;

import java.util.List;

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
         *
         * @param session session
         */
        void p2vConnectSuccess(Session session);
    }

    interface Presenter extends IBasePresenter {

        /**
         * 连接SSH
         *
         * @param sshEntity ssh
         */
        void v2pConnectSsh(SshEntity sshEntity);

        /**
         * 获取符号
         *
         * @return 符号
         */
        List<KeyCodeBean> v2pGetSymbol();
    }

    interface Model extends IBaseModel {
        /**
         * 连接SSH
         *
         * @param sshEntity SSH信息
         * @return 观察者
         */
        Observable<Session> p2mConnectSsh(SshEntity sshEntity);


        /**
         * 关闭连接
         */
        void p2mClose();
    }
}
