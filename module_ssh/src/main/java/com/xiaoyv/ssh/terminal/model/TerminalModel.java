package com.xiaoyv.ssh.terminal.model;

import com.blankj.utilcode.util.ThreadUtils;
import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.xiaoyv.busines.config.SshLoginType;
import com.xiaoyv.busines.exception.RxException;
import com.xiaoyv.busines.room.entity.SshEntity;
import com.xiaoyv.ssh.terminal.contract.TerminalContract;

import java.io.File;

import io.reactivex.rxjava3.core.Observable;

/**
 * TerminalModel
 *
 * @author why
 * @since 2020/12/06
 **/
public class TerminalModel implements TerminalContract.Model {
    private Connection connection;

    @Override
    public Observable<Session> p2mConnectSsh(SshEntity sshEntity) {
        return Observable.create(emitter -> {
            try {
                int port = Integer.parseInt(sshEntity.port);
                connection = new Connection(sshEntity.ip, port);
                connection.connect((sHost, sPort, sEntType, bytes) -> true, 5000, 5000);
                boolean authenticate = false;
                switch (sshEntity.authType) {
                    case SshLoginType.TYPE_NONE:
                        authenticate = connection.authenticateWithNone(sshEntity.account);
                        break;
                    case SshLoginType.TYPE_PASSWORD:
                        authenticate = connection.authenticateWithPassword(sshEntity.account, sshEntity.password);
                        break;
                    case SshLoginType.TYPE_PUBLIC_KEY:
                        authenticate = connection.authenticateWithPublicKey(sshEntity.account, new File(""), sshEntity.password);
                        break;
                    default:
                        break;
                }
                // 连接失败
                if (!authenticate) {
                    emitter.onError(new RxException("SSH 认证失败"));
                    emitter.onComplete();
                    return;
                }
                Session session = connection.openSession();
                session.requestPTY("vt100");
                session.startShell();

                emitter.onNext(session);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
                emitter.onComplete();
            }
        });
    }

    @Override
    public void p2mClose() {
        if (connection != null) {
            ThreadUtils.getCachedPool().execute(() -> {
                connection.close();
                connection = null;
            });
        }
    }
}
