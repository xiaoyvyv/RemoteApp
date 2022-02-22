package com.xiaoyv.rdp.main.contract;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayout;
import com.xiaoyv.busines.base.BaseSubscriber;
import com.xiaoyv.busines.base.IBaseModel;
import com.xiaoyv.busines.base.IBasePresenter;
import com.xiaoyv.busines.base.IBaseView;
import com.xiaoyv.busines.room.entity.RdpEntity;
import com.xiaoyv.ui.listener.SimpleResultListener;

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

        void removeItem(RdpEntity dataBean, int adapterPos);

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

        /**
         * 获取TabLayout
         *
         * @return 分组标签
         */
        TabLayout p2vGetTabLayout();
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

        /**
         * 查询全部分组
         *
         * @param rdpEntities 全部列表
         * @param subscribe   subscribe
         */
        void v2pResolveAllGroup(@NonNull List<RdpEntity> rdpEntities, BaseSubscriber<List<String>> subscribe);

        /**
         * 删除一个配置信息
         *
         * @param dataBean       配置信息
         * @param resultListener 回调
         */
        void v2pDeleteRdp(RdpEntity dataBean, SimpleResultListener<Boolean> resultListener);
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
