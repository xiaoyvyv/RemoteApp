package com.xiaoyv.rdp.main

import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.StringUtils
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.blueprint.exception.RxException
import com.xiaoyv.busines.room.database.DateBaseManger
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.rdp.R
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
            it.onNext(DateBaseManger.get().rdpDao.all)
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

    override fun p2mResolveAllGroup(): Observable<List<String>> {
        return Observable.create {
            // 数据库内容
            val rdpEntities = DateBaseManger.get().rdpDao.all
            if (rdpEntities.isNullOrEmpty()) {
                it.onError(RxException("rdp record list is null!"))
                return@create
            }

            val groups = arrayListOf<String>()

            // 遍历
            rdpEntities.forEach { entity ->
                var group = entity.group
                if (StringUtils.isEmpty(group)) {
                    group = StringUtils.getString(R.string.rdp_main_group)
                }
                if (!groups.contains(group)) {
                    groups.add(group)
                }
            }

            // 排序
            groups.sortWith { obj: String, anotherString ->
                obj.compareTo(anotherString)
            }

            it.onNext(groups)
            it.onComplete()
        }
    }

    override fun p2mQueryLocalRdpByGroup(group: String): Observable<List<RdpEntity>> {
        return Observable.create {
            it.onNext(DateBaseManger.get().rdpDao.getAllByGroup(group))
            it.onComplete()
        }
    }
}