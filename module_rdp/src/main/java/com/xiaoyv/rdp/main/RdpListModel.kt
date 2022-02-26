package com.xiaoyv.rdp.main

import com.blankj.utilcode.util.GsonUtils
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.blueprint.exception.RxException
import com.xiaoyv.busines.room.database.DateBaseManger
import com.xiaoyv.busines.room.entity.RdpEntity
import io.reactivex.rxjava3.core.Observable

/**
 * RdpListModel
 *
 * @author why
 * @since 2020/11/29
 */
class RdpListModel : RdpListContract.Model {
    override fun p2mQueryLocalRdp(): Observable<List<RdpEntity>> {
        return Observable.create {
            it.onNext(DateBaseManger.get().rdpDao.queryAll())
            it.onComplete()
        }
    }

    override fun p2mDeleteRdp(dataBean: RdpEntity): Observable<Boolean> {
        return Observable.create {
            // 清除RDP配置数据库当前条目
            val rdpBookmark = GsonUtils.fromJson(dataBean.configStr, RdpConfig::class.java)

            // 清除 RdpEntity 当前条目
            DateBaseManger.get().rdpDao.delete(dataBean)

            it.onNext(true)
            it.onComplete()
        }
    }

    override fun p2mQueryGroup(): Observable<List<String>> {
        return Observable.create {
            // 数据库内容
            val groupList = DateBaseManger.get().rdpDao.queryGroup().toMutableList()
            if (groupList.isNullOrEmpty()) {
                it.onError(RxException("rdp record list is null!"))
                return@create
            }

            it.onNext(groupList)
            it.onComplete()
        }
    }

    override fun p2mQueryLocalRdpByGroup(group: String): Observable<List<RdpEntity>> {
        return Observable.create {
            it.onNext(DateBaseManger.get().rdpDao.queryByGroup(group))
            it.onComplete()
        }
    }
}