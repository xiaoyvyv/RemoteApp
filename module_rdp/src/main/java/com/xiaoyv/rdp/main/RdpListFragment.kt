package com.xiaoyv.rdp.main

import android.view.LayoutInflater
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.*
import com.chad.library.adapter.base.BaseBinderAdapter
import com.github.nukc.stateview.StateView
import com.google.android.material.tabs.TabLayout
import com.xiaoyv.blueprint.base.binding.BaseMvpBindingFragment
import com.xiaoyv.blueprint.base.rxjava.event.RxEvent
import com.xiaoyv.business.config.NavigationPath
import com.xiaoyv.business.room.entity.RdpEntity
import com.xiaoyv.business.rx.RxEventTag
import com.xiaoyv.desktop.rdp.R
import com.xiaoyv.desktop.rdp.databinding.RdpFragmentMainBinding
import com.xiaoyv.rdp.add.AddRdpActivity
import com.xiaoyv.rdp.screen.view.ScreenActivity
import com.xiaoyv.rdp.setting.RdpSettingActivity
import com.xiaoyv.ui.listener.SimpleRefreshListener
import com.xiaoyv.ui.listener.SimpleTabSelectListener
import com.xiaoyv.widget.binder.setOnItemClickListener
import com.xiaoyv.widget.dialog.UiOptionsDialog
import com.xiaoyv.widget.utils.doOnBarClick
import com.xiaoyv.widget.utils.overScrollV

/**
 * RdpFragment
 *
 * @author why
 * @since 2020/11/29
 */
@Route(path = NavigationPath.PATH_RDP_FRAGMENT)
class RdpListFragment :
    BaseMvpBindingFragment<RdpFragmentMainBinding, RdpListContract.View, RdpListPresenter>(),
    RdpListContract.View {

    private lateinit var multiTypeAdapter: BaseBinderAdapter
    private lateinit var rdpBinder: RdpListBindingBinder

    private var currentGroupIndex = 0

    override fun createContentBinding(layoutInflater: LayoutInflater): RdpFragmentMainBinding {
        return RdpFragmentMainBinding.inflate(layoutInflater)
    }

    override fun createPresenter() = RdpListPresenter()

    override fun initView() {
        stateController.fitTitleAndStatusBar = true
        binding.toolbar.title = StringUtils.getString(R.string.rdp_main_title)
        binding.toolbar.setRightIcon(
            R.drawable.ui_icon_search,
            onBarClickListener = doOnBarClick { view, which ->
                RdpSettingActivity.openSelf(RdpSettingActivity.TYPE_SETTING_UI)
            })

//        binding.toolbar
//            .setTitle(StringUtils.getString(R.string.rdp_main_title))
//            .setEndIcon(R.drawable.ui_icon_search)
//            .setEndClickListener { v: View? ->
//                RdpSettingActivity.openSelf(RdpSettingActivity.TYPE_SETTING_UI)
//                RdpSettingActivity.openSelf(RdpSettingActivity.TYPE_SETTING_POWER)
//                RdpSettingActivity.openSelf(RdpSettingActivity.TYPE_SETTING_SECURITY)
//            }

        binding.rvContent.overScrollV()
    }

    override fun initData() {
        rdpBinder = RdpListBindingBinder()
        multiTypeAdapter = BaseBinderAdapter()
        multiTypeAdapter.addItemBinder(rdpBinder)

        binding.rvContent.adapter = multiTypeAdapter

        addReceiveEventTag(RxEventTag.EVENT_ADD_RDP_SUCCESS)
    }

    override fun initListener() {
        rdpBinder.setOnItemClickListener { _, dataBean, position, isLongClick ->
            // 连接
            if (!isLongClick) {
                p2vShowLoading("正在校验主机连通性")
                presenter.v2pCheckHost(dataBean)
                return@setOnItemClickListener
            }

            val optionsDialog = UiOptionsDialog.Builder().apply {
                itemDataList = StringUtils.getStringArray(R.array.ui_context_menu).toList()
                itemLastColor = ColorUtils.getColor(R.color.ui_status_error)

                onOptionsClickListener = { dialog, _, index ->
                    dialog.dismiss()
                    when (index) {
                        0 -> {
                            p2vShowLoading("正在校验主机连通性")
                            presenter.v2pCheckHost(dataBean)
                        }
                        1 -> AddRdpActivity.openSelf(dataBean)
                        2 -> removeItem(dataBean, position)
                    }
                    true
                }

            }.create()
            optionsDialog.show(this)
        }

        binding.fabAdd.setOnClickListener {
            ARouter.getInstance()
                .build(NavigationPath.PATH_RDP_ADD_ACTIVITY)
                .navigation()
        }

        binding.tlGroup.addOnTabSelectedListener(object : SimpleTabSelectListener() {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentGroupIndex = tab.position
                presenter.v2pQueryLocalRdpByGroup(tab.text.toString())
            }
        })

        // 刷新
        binding.rvContent.overScrollV()
            .setOverScrollUpdateListener(object : SimpleRefreshListener() {
                override fun onRefresh() {
                    val tab = binding.tlGroup.getTabAt(currentGroupIndex) ?: return
                    val group = tab.text.toString()
                    presenter.v2pQueryLocalRdpByGroup(group)
                }
            })
    }


    override fun initFinish() {
        // 查询
        presenter.v2pQueryGroup()
    }

    override fun onReceiveRxEvent(rxEvent: RxEvent, rxEventTag: String) {
        when (rxEventTag) {
            RxEventTag.EVENT_ADD_RDP_SUCCESS -> {
                // 更新查询
                presenter.v2pQueryGroup()
            }
        }
    }

    override fun removeItem(dataBean: RdpEntity, adapterPos: Int) {
        multiTypeAdapter.data.removeAt(adapterPos)
        multiTypeAdapter.notifyItemRemoved(adapterPos)

        // 删除后重新查询
        presenter.v2pDeleteRdp(dataBean)
    }

    override fun p2vCheckHostResult(rdpEntity: RdpEntity, success: Boolean) {
        p2vHideLoading()
        if (success) {
            ThreadUtils.runOnUiThreadDelayed({
                ScreenActivity.openSelf(rdpEntity)
            }, 200)
        } else {
            ToastUtils.showShort("%s:%s 连通性校验失败", rdpEntity.ip, rdpEntity.port)
        }
    }

    /**
     * 全部分组
     */
    override fun p2vShowRdpGroups(groupNames: List<String>) {
        binding.tlGroup.removeAllTabs()
        if (ObjectUtils.isEmpty(groupNames)) {
            binding.tlGroup.visibility = View.GONE
            return
        }

        if (currentGroupIndex >= groupNames.size) {
            currentGroupIndex = groupNames.size - 1
        }

        binding.tlGroup.visibility = View.VISIBLE
        groupNames.forEachIndexed { index, group ->
            val newTab = binding.tlGroup.newTab()
            binding.tlGroup.addTab(newTab.setText(group), currentGroupIndex == index)
        }
        stateController.showNormalView()
    }

    override fun p2vShowRdpListByGroup(rdpEntities: List<RdpEntity>, group: String) {
        multiTypeAdapter.setList(rdpEntities)
        multiTypeAdapter.notifyItemRangeChanged(0, rdpEntities.size)
    }

    /**
     * 删除成功
     */
    override fun p2vDeleteRdpResult(success: Boolean) {
        // 重新查询
        presenter.v2pQueryGroup()
    }

    override fun p2vGetTabLayout(): TabLayout {
        return binding.tlGroup
    }

    override fun p2vClickStatusView(stateView: StateView, view: View) {
        ARouter.getInstance().build(NavigationPath.PATH_RDP_ADD_ACTIVITY).navigation()
    }
}