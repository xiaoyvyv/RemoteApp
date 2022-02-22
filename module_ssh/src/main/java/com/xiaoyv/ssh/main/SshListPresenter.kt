package com.xiaoyv.ssh.main

import com.blankj.utilcode.util.ObjectUtils
import com.blankj.utilcode.util.StringUtils
import com.xiaoyv.blueprint.base.ImplBasePresenter
import com.xiaoyv.blueprint.base.subscribesWithPresenter
import com.xiaoyv.busines.room.entity.SshEntity
import com.xiaoyv.ssh.R

/**
 * SshListPresenter
 *
 * @author why
 * @since 2020/11/29
 */
class SshListPresenter : ImplBasePresenter<SshListContract.View>(), SshListContract.Presenter {
    private val model: SshListContract.Model = SshListModel()

    override fun v2pQueryLocalSshByGroup(group: String) {
        model.p2mQueryLocalSshByGroup(group)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    if (ObjectUtils.isEmpty(it)) {
                        requireView.stateController.showEmptyView()
                        return@subscribesWithPresenter
                    }
                    requireView.p2vShowSshListByGroup(it, group)
                },
                onError = {
                    // 当前组没有配置的桌面则显示暂无内容
                    requireView.stateController.showEmptyView()
                }
            )
    }


    override fun v2pResolveSshByGroup() {
        model.p2mResolveAllGroup()
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    requireView.p2vShowSshGroups(it)
                },
                onError = {
                    // 没有配置的桌面则显示去创建
                    requireView.stateController.showRetryView(
                        StringUtils.getString(R.string.ssh_main_list_empty),
                        StringUtils.getString(R.string.ssh_main_add_now),
                        R.drawable.ui_pic_status_empty
                    )
                    requireView.p2vGetTabLayout().removeAllTabs()
                }
            )
    }

    override fun v2pDeleteSsh(dataBean: SshEntity) {
        model.p2mDeleteSsh(dataBean)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    requireView.p2vDeleteSshResult(it)
                },
                onError = {

                }
            )
    }
}