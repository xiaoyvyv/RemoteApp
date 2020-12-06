package com.xiaoyv.ssh.main.model;

import com.xiaoyv.busines.room.database.DateBaseManger;
import com.xiaoyv.busines.room.entity.SshEntity;
import com.xiaoyv.ssh.main.contract.SshListContract;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * SshListModel
 *
 * @author why
 * @since 2020/11/29
 **/
public class SshListModel implements SshListContract.Model {

    @Override
    public Observable<List<SshEntity>> p2mQueryLocalSsh() {
        return Observable.create(emitter -> {
            emitter.onNext(DateBaseManger.get().getSshDao().getAll());
            emitter.onComplete();
        });
    }

    @Override
    public Observable<List<SshEntity>> p2mQueryLocalSshByGroup(String group) {
        return Observable.create(emitter -> {
            emitter.onNext(DateBaseManger.get().getSshDao().getAllByGroup(group));
            emitter.onComplete();
        });
    }
}
