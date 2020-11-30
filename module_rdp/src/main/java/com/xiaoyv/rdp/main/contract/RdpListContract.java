package com.xiaoyv.rdp.main.contract;

import com.xiaoyv.busines.base.IBaseModel;
import com.xiaoyv.busines.base.IBasePresenter;
import com.xiaoyv.busines.base.IBaseView;
import com.xiaoyv.busines.room.entity.RdpEntity;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * RdpListContract
 *
 * @author why
 * @since 2020/11/29
 **/
public interface RdpListContract {
    interface View extends IBaseView {

        /**
         * Rdp 列表
         *
         * @param rdpEntities 列表
         */
        void p2vQueryLocalRdp(List<RdpEntity> rdpEntities);

        /**
         * Rdp 列表
         *
         * @param rdpEntities 列表
         * @param group       组
         */
        void p2vQueryLocalRdpByGroup(List<RdpEntity> rdpEntities, String group);
    }

    interface Presenter extends IBasePresenter {
        /**
         * 获取全部 rdp
         */
        void v2pQueryLocalRdp();

        /**
         * 根据组获取全部 rdp
         *
         * @param group 组
         */
        void v2pQueryLocalRdpByGroup(String group);
    }

    interface Model extends IBaseModel {

        /**
         * 数据库查询 rdp
         *
         * @return 列表
         */
        Observable<List<RdpEntity>> p2mQueryLocalRdp();

        /**
         * 数据库查询 rdp
         *
         * @param group 组
         * @return 列表
         */
        Observable<List<RdpEntity>> p2mQueryLocalRdpByGroup(String group);
    }
}
