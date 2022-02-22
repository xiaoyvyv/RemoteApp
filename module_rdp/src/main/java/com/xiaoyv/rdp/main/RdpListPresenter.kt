package com.xiaoyv.rdp.main

import com.blankj.utilcode.util.ObjectUtils
import com.blankj.utilcode.util.StringUtils
import com.xiaoyv.blueprint.base.ImplBasePresenter
import com.xiaoyv.blueprint.base.subscribesWithPresenter
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.rdp.R

/**
 * RdpListPresenter
 *
 * @author why
 * @since 2020/11/29
 */
class RdpListPresenter : ImplBasePresenter<RdpListContract.View>(), RdpListContract.Presenter {
    private val model = RdpListModel()

    override fun v2pQueryLocalRdpByGroup(group: String) {
        model.p2mQueryLocalRdpByGroup(group)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    if (ObjectUtils.isEmpty(it)) {
                        requireView.stateController.showEmptyView()
                        return@subscribesWithPresenter
                    }
                    requireView.p2vShowRdpListByGroup(it, group)
                },
                onError = {
                    // 当前组没有配置的桌面则显示暂无内容
                    requireView.stateController.showEmptyView()
                }
            )
    }


    override fun v2pResolveRdpByGroup() {
        model.p2mResolveAllGroup()
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    requireView.p2vShowRdpGroups(it)
                },
                onError = {
                    // 没有配置的桌面则显示去创建
                    requireView.stateController.showRetryView(
                        StringUtils.getString(R.string.rdp_main_list_empty),
                        StringUtils.getString(R.string.rdp_main_add_now),
                        R.drawable.ui_pic_status_empty
                    )
                    requireView.p2vGetTabLayout().removeAllTabs()
                }
            )
    }

    override fun v2pDeleteRdp(dataBean: RdpEntity) {
        model.p2mDeleteRdp(dataBean)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    requireView.p2vDeleteRdpResult(it)
                },
                onError = {

                }
            )
    }
}