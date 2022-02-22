package com.xiaoyv.ssh.main.contract;

import androidx.annotation.NonNull;

import com.google.android.material.tabs.TabLayout;
import com.xiaoyv.busines.room.entity.SshEntity;
import com.xiaoyv.ui.listener.SimpleResultListener;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;

/**
 * SshListContract
 *
 * @author why
 * @since 2020/11/29
 **/
public interface SshListContract {
    interface View extends IBaseView {

        void removeItem(SshEntity dataBean, int adapterPos);

        /**
         * Ssh 列表
         *
         * @param SshEntities 列表
         */
        void p2vQueryLocalSsh(List<SshEntity> SshEntities);

        /**
         * Ssh 列表
         *
         * @param SshEntities 列表
         * @param group       组
         */
        void p2vQueryLocalSshByGroup(List<SshEntity> SshEntities, String group);

        /**
         * 获取TabLayout
         *
         * @return 分组标签
         */
        TabLayout p2vGetTabLayout();
    }

    interface Presenter extends IBasePresenter {
        /**
         * 获取全部 Ssh
         */
        void v2pQueryLocalSsh();

        /**
         * 根据组获取全部 Ssh
         *
         * @param group 组
         */
        void v2pQueryLocalSshByGroup(String group);

        /**
         * 查询全部分组
         *
         * @param SshEntities 全部列表
         * @param subscribe   subscribe
         */
        void v2pResolveAllGroup(@NonNull List<SshEntity> SshEntities, BaseSubscriber<List<String>> subscribe);

        /**
         * 删除一个配置信息
         *
         * @param dataBean       配置信息
         * @param resultListener 回调
         */
        void v2pDeleteSsh(SshEntity dataBean, SimpleResultListener<Boolean> resultListener);
    }

    interface Model {

        /**
         * 数据库查询 Ssh
         *
         * @return 列表
         */
        Observable<List<SshEntity>> p2mQueryLocalSsh();

        /**
         * 数据库查询 Ssh
         *
         * @param group 组
         * @return 列表
         */
        Observable<List<SshEntity>> p2mQueryLocalSshByGroup(String group);
    }
}
