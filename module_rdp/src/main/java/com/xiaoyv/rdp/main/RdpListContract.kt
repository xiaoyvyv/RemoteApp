package com.xiaoyv.rdp.main

import com.google.android.material.tabs.TabLayout
import com.xiaoyv.blueprint.base.IBasePresenter
import com.xiaoyv.blueprint.base.IBaseView
import com.xiaoyv.busines.room.entity.RdpEntity
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

        fun p2vShowRdpGroups(groupNames: List<String>)

        /**
         * 显示根据分组名称查询 Rdp 列表
         *
         * @param rdpEntities 列表
         * @param group       组
         */
        fun p2vShowRdpListByGroup(rdpEntities: List<RdpEntity>, group: String)

        /**
         * 删除结果
         */
        fun p2vDeleteRdpResult(success: Boolean)

        /**
         * 获取TabLayout
         *
         * @return 分组标签
         */
        fun p2vGetTabLayout(): TabLayout

        /**
         * 检测连通性
         */
        fun p2vCheckHostResult(rdpEntity: RdpEntity, success: Boolean)

    }

    interface Presenter : IBasePresenter {
        /**
         * 根据组获取全部 rdp
         *
         * @param group 组
         */
        fun v2pQueryLocalRdpByGroup(group: String)

        /**
         * 查询全部分组数据
         */
        fun v2pQueryGroup()

        /**
         * 删除一个配置信息
         *
         * @param dataBean       配置信息
         */
        fun v2pDeleteRdp(dataBean: RdpEntity)

        fun v2pCheckHost(rdpEntity: RdpEntity)
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
        fun p2mQueryGroup(): Observable<List<String>>

    }
}