package com.xiaoyv.rdp.main

import android.view.LayoutInflater
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ObjectUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.google.android.material.tabs.TabLayout
import com.xiaoyv.blueprint.base.binding.BaseMvpBindingFragment
import com.xiaoyv.ui.base.setOnItemClickListener
import com.xiaoyv.busines.config.NavigationPath
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.rdp.R
import com.xiaoyv.rdp.add.AddRdpActivity
import com.xiaoyv.rdp.databinding.RdpFragmentMainBinding
import com.xiaoyv.rdp.screen.view.ScreenActivity
import com.xiaoyv.ui.dialog.OptionsDialog
import com.xiaoyv.ui.dialog.OptionsDialogItemBinder
import com.xiaoyv.ui.listener.SimpleRefreshListener
import com.xiaoyv.ui.listener.SimpleTabSelectListener
import com.xiaoyv.widget.utils.overScrollV
import me.everything.android.ui.overscroll.IOverScrollDecor

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

    private var scrollDecor: IOverScrollDecor? = null

    override fun createContentBinding(layoutInflater: LayoutInflater): RdpFragmentMainBinding {
        return RdpFragmentMainBinding.inflate(layoutInflater)
    }

    override fun createPresenter() = RdpListPresenter()

    override fun initView() {
//        binding.toolbar
//            .setTitle(StringUtils.getString(R.string.rdp_main_title))
//            .setEndIcon(R.drawable.ui_icon_search)
//            .setEndClickListener { v: View? ->
//                RdpSettingActivity.openSelf(RdpSettingActivity.TYPE_SETTING_UI)
//                RdpSettingActivity.openSelf(RdpSettingActivity.TYPE_SETTING_POWER)
//                RdpSettingActivity.openSelf(RdpSettingActivity.TYPE_SETTING_SECURITY)
//            }
        scrollDecor = binding.rvContent.overScrollV()
    }

    override fun initData() {
        rdpBinder = RdpListBindingBinder()
        multiTypeAdapter = BaseBinderAdapter()
        multiTypeAdapter.addItemBinder(rdpBinder)

        binding.rvContent.adapter = multiTypeAdapter
    }

    override fun initListener() {
        rdpBinder.setOnItemClickListener { _, dataBean, position, isLongClick ->
            // 连接
            if (!isLongClick) {
                ScreenActivity.openSelf(dataBean)
                return@setOnItemClickListener
            }
            val optionsDialog = OptionsDialog.get(activity)
            optionsDialog.setCancelable(true)
            optionsDialog.setOptions(*StringUtils.getStringArray(R.array.ui_context_menu))
            optionsDialog.setLastTextColor(ColorUtils.getColor(R.color.ui_status_error))
            optionsDialog.setOnItemChildClickListener(object :OptionsDialogItemBinder.OnItemChildClickListener{
                override fun onItemChildClick(position: Int) {
                    when (position) {
                        0 -> ScreenActivity.openSelf(dataBean)
                        1 -> AddRdpActivity.openSelf(dataBean)
                        2 -> removeItem(dataBean, position)
                    }
                }
            })
            optionsDialog.show()
        }

        binding.fabAdd.setOnClickListener {
            ARouter.getInstance()
                .build(NavigationPath.PATH_RDP_ADD_ACTIVITY)
                .navigation()
        }

        binding.tlGroup.addOnTabSelectedListener(object : SimpleTabSelectListener() {
            override fun onTabSelected(tab: TabLayout.Tab) {
                presenter.v2pQueryLocalRdpByGroup(tab.text.toString())
            }
        })

        // 刷新
        scrollDecor?.setOverScrollUpdateListener(object : SimpleRefreshListener() {
            override fun onRefresh() {
                val tab = binding.tlGroup.getTabAt(binding.tlGroup.selectedTabPosition) ?: return
                val group = tab.text.toString()
                presenter.v2pQueryLocalRdpByGroup(group)
            }
        })
    }

    override fun removeItem(dataBean: RdpEntity, adapterPos: Int) {
        multiTypeAdapter.data.removeAt(adapterPos)
        multiTypeAdapter.notifyItemRemoved(adapterPos)

        // 删除后重新查询
        presenter.v2pDeleteRdp(dataBean)
    }

    override fun p2vShowRdpGroups(groupNames: List<String>) {
        binding.tlGroup.removeAllTabs()
        if (ObjectUtils.isEmpty(groupNames)) {
            binding.tlGroup.visibility = View.GONE
            return
        }
        binding.tlGroup.visibility = View.VISIBLE
        for (group in groupNames) {
            binding.tlGroup.addTab(binding.tlGroup.newTab().setText(group))
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
        // 重新查询查询
        presenter.v2pResolveRdpByGroup()
    }

    override fun onResumeExceptFirst() {
        // 重新查询查询
        presenter.v2pResolveRdpByGroup()
    }


    override fun p2vGetTabLayout(): TabLayout {
        return binding.tlGroup
    }

    override fun p2vClickStatusView() {
        ARouter.getInstance().build(NavigationPath.PATH_RDP_ADD_ACTIVITY).navigation()
    }
}