package com.xiaoyv.ssh.terminal.model;

import com.trilead.ssh2.Connection;
import com.trilead.ssh2.Session;
import com.xiaoyv.busines.config.SshLoginType;
import com.xiaoyv.busines.exception.RxException;
import com.xiaoyv.busines.room.entity.SshEntity;
import com.xiaoyv.ssh.terminal.contract.TerminalContract;

import java.io.File;
import java.util.LinkedHashMap;

import io.reactivex.rxjava3.core.Observable;

/**
 * TerminalModel
 *
 * @author why
 * @since 2020/12/06
 **/
public class TerminalModel implements TerminalContract.Model {
    public static final LinkedHashMap<SshEntity, Session> MAP_SESSIONS = new LinkedHashMap<>();

    @Override
    public Observable<Session> p2mConnectSsh(SshEntity sshEntity) {
        return Observable.create(emitter -> {
            try {
                if (MAP_SESSIONS.containsKey(sshEntity)) {
                    Session session = MAP_SESSIONS.get(sshEntity);
                    if (session != null) {
                        emitter.onNext(session);
                        emitter.onComplete();
                        return;
                    }
                }
                int port = Integer.parseInt(sshEntity.port);
                Connection connection = new Connection(sshEntity.ip, port);
                connection.connect();
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

                MAP_SESSIONS.put(sshEntity, session);
                emitter.onNext(session);
                emitter.onComplete();
            } catch (Exception e) {
                emitter.onError(e);
                emitter.onComplete();
            }
        });
    }
}
