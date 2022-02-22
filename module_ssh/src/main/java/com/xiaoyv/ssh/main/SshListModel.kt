package com.xiaoyv.ssh.main

import com.blankj.utilcode.util.StringUtils
import com.xiaoyv.blueprint.exception.RxException
import com.xiaoyv.busines.room.database.DateBaseManger
import com.xiaoyv.busines.room.entity.SshEntity
import com.xiaoyv.ssh.R
import io.reactivex.rxjava3.core.Observable

/**
 * SshListModel
 *
 * @author why
 * @since 2020/11/29
 */
class SshListModel : SshListContract.Model {
    override fun p2mQueryLocalSsh(): Observable<List<SshEntity>> {
        return Observable.create {
            it.onNext(DateBaseManger.get().sshDao.all)
            it.onComplete()
        }
    }

    override fun p2mResolveAllGroup(): Observable<List<String>> {
        return Observable.create {
            // 数据库内容
            val rdpEntities = DateBaseManger.get().sshDao.all
            if (rdpEntities.isNullOrEmpty()) {
                it.onError(RxException("ssh record list is null!"))
                return@create
            }

            val groups = arrayListOf<String>()

            // 遍历
            rdpEntities.forEach { entity ->
                var group = entity.group
                if (StringUtils.isEmpty(group)) {
                    group = StringUtils.getString(R.string.ssh_main_group)
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

    override fun p2mDeleteSsh(sshEntity: SshEntity): Observable<Boolean> {
        return Observable.create {
            DateBaseManger.get().sshDao.delete(sshEntity)
            it.onNext(true)
            it.onComplete()
        }
    }

    override fun p2mQueryLocalSshByGroup(group: String): Observable<List<SshEntity>> {
        return Observable.create {
            it.onNext(DateBaseManger.get().sshDao.getAllByGroup(group))
            it.onComplete()
        }
    }
}