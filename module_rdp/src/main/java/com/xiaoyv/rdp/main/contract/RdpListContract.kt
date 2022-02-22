package com.xiaoyv.rdp.main.contract

import com.google.android.material.tabs.TabLayout
import com.xiaoyv.blueprint.base.IBasePresenter
import com.xiaoyv.blueprint.base.IBaseView
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.ui.listener.SimpleResultListener
import io.reactivex.rxjava3.core.Observable

/**
 * RdpListContract
 *
 * @author why
 * @since 2020/11/29
 */
interface RdpListContract {
    interface View : IBaseView {
        fun removeItem(dataBean: RdpEntity, adapterPos: Int)

        /**
         * Rdp 列表
         *
         * @param rdpEntities 列表
         */
        fun p2vQueryLocalRdp(rdpEntities: List<RdpEntity>)

        /**
         * Rdp 列表
         *
         * @param rdpEntities 列表
         * @param group       组
         */
        fun p2vQueryLocalRdpByGroup(rdpEntities: List<RdpEntity>, group: String)

        /**
         * 获取TabLayout
         *
         * @return 分组标签
         */
        fun p2vGetTabLayout(): TabLayout
    }

    interface Presenter : IBasePresenter {
        /**
         * 获取全部 rdp
         */
        fun v2pQueryLocalRdp()

        /**
         * 根据组获取全部 rdp
         *
         * @param group 组
         */
        fun v2pQueryLocalRdpByGroup(group: String)

        /**
         * 查询全部分组
         *
         * @param rdpEntities 全部列表
         */
        fun v2pResolveAllGroup(rdpEntities: List<RdpEntity>)

        /**
         * 删除一个配置信息
         *
         * @param dataBean       配置信息
         */
        fun v2pDeleteRdp(dataBean: RdpEntity)
    }

    interface Model {
        /**
         * 数据库查询 rdp
         *
         * @return 列表
         */
        fun p2mQueryLocalRdp(): Observable<List<RdpEntity>>

        /**
         * 数据库查询 rdp
         *
         * @param group 组
         * @return 列表
         */
        fun p2mQueryLocalRdpByGroup(group: String): Observable<List<RdpEntity>>
        fun p2mDeleteRdp(dataBean: RdpEntity): Observable<Boolean>
        fun p2mResolveAllGroup(rdpEntities: List<RdpEntity>): Observable<List<String>>

    }
}