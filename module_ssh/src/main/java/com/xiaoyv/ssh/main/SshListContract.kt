package com.xiaoyv.ssh.main

import com.google.android.material.tabs.TabLayout
import com.xiaoyv.blueprint.base.IBasePresenter
import com.xiaoyv.blueprint.base.IBaseView
import com.xiaoyv.busines.room.entity.SshEntity
import io.reactivex.rxjava3.core.Observable

/**
 * SshListContract
 *
 * @author why
 * @since 2020/11/29
 */
interface SshListContract {
    interface View : IBaseView {
        fun removeItem(dataBean: SshEntity, adapterPos: Int)

        fun p2vShowSshGroups(groupNames: List<String>)

        /**
         * Ssh 列表
         *
         * @param sshEntities 列表
         * @param group       组
         */
        fun p2vShowSshListByGroup(sshEntities: List<SshEntity>, group: String)

        /**
         * 获取TabLayout
         *
         * @return 分组标签
         */
        fun p2vGetTabLayout(): TabLayout

        /**
         * 是否删除成功
         */
        fun p2vDeleteSshResult(success: Boolean)
    }

    interface Presenter : IBasePresenter {
        /**
         * 查询全部分组
         */
        fun v2pResolveSshByGroup()

        /**
         * 根据组获取全部 Ssh
         *
         * @param group 组
         */
        fun v2pQueryLocalSshByGroup(group: String)

        /**
         * 删除一个配置信息
         *
         * @param dataBean       配置信息
         */
        fun v2pDeleteSsh(dataBean: SshEntity)
    }

    interface Model {
        /**
         * 数据库查询 Ssh
         *
         * @return 列表
         */
        fun p2mQueryLocalSsh(): Observable<List<SshEntity>>

        /**
         * 数据库查询 Ssh
         *
         * @param group 组
         * @return 列表
         */
        fun p2mQueryLocalSshByGroup(group: String): Observable<List<SshEntity>>

        fun p2mResolveAllGroup(): Observable<List<String>>

        /**
         * 删除
         */
        fun p2mDeleteSsh(sshEntity: SshEntity): Observable<Boolean>
    }
}