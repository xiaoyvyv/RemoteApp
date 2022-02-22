package com.xiaoyv.rdp.main.model;

import com.xiaoyv.busines.room.database.DateBaseManger;
import com.xiaoyv.busines.room.entity.RdpEntity;
import com.xiaoyv.rdp.main.contract.RdpListContract;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * RdpListModel
 *
 * @author why
 * @since 2020/11/29
 **/
public class RdpListModel implements RdpListContract.Model {

    @Override
    public Observable<List<RdpEntity>> p2mQueryLocalRdp() {
        return Observable.create(emitter -> {
            emitter.onNext(DateBaseManger.get().getRdpDao().getAll());
            emitter.onComplete();
        });
    }

    @Override
    public Observable<List<RdpEntity>> p2mQueryLocalRdpByGroup(String group) {
        return Observable.create(emitter -> {
            emitter.onNext(DateBaseManger.get().getRdpDao().getAllByGroup(group));
            emitter.onComplete();
        });
    }
}
