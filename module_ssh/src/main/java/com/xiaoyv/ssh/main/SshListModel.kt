package com.xiaoyv.ssh.main

import com.xiaoyv.blueprint.exception.RxException
import com.xiaoyv.business.room.database.DateBaseManger
import com.xiaoyv.business.room.entity.SshEntity

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
            it.onNext(DateBaseManger.get().sshDao.queryAll())
            it.onComplete()
        }
    }

    override fun p2mQueryGroup(): Observable<List<String>> {
        return Observable.create {
            // 数据库内容
            val groupList = DateBaseManger.get().sshDao.queryGroup().toMutableList()
            if (groupList.isNullOrEmpty()) {
                it.onError(RxException("rdp record list is null!"))
                return@create
            }

            it.onNext(groupList)
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
            it.onNext(DateBaseManger.get().sshDao.queryByGroup(group))
            it.onComplete()
        }
    }
}