package com.xiaoyv.ssh.main

import com.blankj.utilcode.util.ObjectUtils
import com.blankj.utilcode.util.StringUtils
import com.xiaoyv.blueprint.base.ImplBasePresenter
import com.xiaoyv.blueprint.base.subscribesWithPresenter
import com.xiaoyv.busines.room.entity.SshEntity
import com.xiaoyv.busines.utils.HostVerifyUtils
import com.xiaoyv.desktop.ssh.R
import com.xiaoyv.ssh.terminal.TerminalActivity


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

    override fun v2pQueryGroup() {
        model.p2mQueryGroup()
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
                        R.drawable.ui_pic_status_empty_normal
                    )
                    requireView.p2vGetTabLayout().removeAllTabs()
                }
            )
    }

    override fun v2pCheckHost(sshEntity: SshEntity) {
        HostVerifyUtils.verifyHost(sshEntity.ip, sshEntity.port)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    requireView.p2vCheckHostResult(sshEntity, it)
                },
                onError = {
                    requireView.p2vCheckHostResult(sshEntity, false)
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