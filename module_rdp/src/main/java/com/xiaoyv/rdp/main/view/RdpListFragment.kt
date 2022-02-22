package com.xiaoyv.rdp.main.view

import android.view.LayoutInflater
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ObjectUtils
import com.blankj.utilcode.util.StringUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.google.android.material.tabs.TabLayout
import com.xiaoyv.blueprint.base.binding.BaseMvpBindingFragment
import com.xiaoyv.busines.base.BaseItemBinder
import com.xiaoyv.busines.config.NavigationPath
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.rdp.databinding.RdpFragmentMainBinding
import com.xiaoyv.rdp.main.adapter.RdpListBinder
import com.xiaoyv.rdp.main.contract.RdpListContract
import com.xiaoyv.rdp.main.presenter.RdpListPresenter
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
    private lateinit var rdpBinder: RdpListBinder

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
        rdpBinder = RdpListBinder()
        multiTypeAdapter = MultiTypeAdapter()
        multiTypeAdapter.register<RdpEntity>(RdpEntity::class.java, rdpBinder)
        binding!!.rvContent.adapter = multiTypeAdapter
    }

     override fun initListener() {
        rdpBinder.setOnItemChildClickListener(BaseItemBinder.OnItemChildClickListener<RdpEntity> { view: View?, dataBean: RdpEntity, adapterPos: Int, longClick: Boolean ->
            // 连接
            if (!longClick) {
                ScreenActivity.openSelf(dataBean)
                return@setOnItemChildClickListener
            }
            val optionsDialog: OptionsDialog = OptionsDialog.get(activity)
            optionsDialog.setCancelable(true)
            optionsDialog.setOptions(*StringUtils.getStringArray(R.array.ui_context_menu))
            optionsDialog.setLastTextColor(ColorUtils.getColor(R.color.ui_status_error))
            optionsDialog.show()
            optionsDialog.setOnItemChildClickListener(OptionsDialogItemBinder.OnItemChildClickListener { position: Int ->
                when (position) {
                    0 -> ScreenActivity.openSelf(dataBean)
                    1 -> AddRdpActivity.openSelf(dataBean)
                    2 -> removeItem(dataBean, adapterPos)
                }
            })
        })
        binding!!.fabAdd.setOnClickListener { v: View? ->
            ARouter.getInstance()
                .build(NavigationPath.PATH_RDP_ADD_ACTIVITY)
                .navigation()
        }
        binding!!.tlGroup.addOnTabSelectedListener(object : SimpleTabSelectListener() {
            override fun onTabSelected(tab: TabLayout.Tab) {
                presenter.v2pQueryLocalRdpByGroup(tab.text.toString())
            }
        })

        // 刷新
        scrollDecor.setOverScrollUpdateListener(object : SimpleRefreshListener() {
            override fun onRefresh() {
                val tab = binding!!.tlGroup.getTabAt(
                    binding!!.tlGroup.selectedTabPosition
                )
                if (tab != null) {
                    val group = tab.text.toString()
                    presenter.v2pQueryLocalRdpByGroup(group)
                }
            }
        })
    }

    override fun removeItem(dataBean: RdpEntity, adapterPos: Int) {
        multiTypeAdapter.items.removeAt(adapterPos)
        multiTypeAdapter.notifyItemRemoved(adapterPos)
        // 删除后重新查询
        presenter.v2pDeleteRdp(dataBean) { result -> presenter.v2pQueryLocalRdp() }
    }

    override fun onResume() {
        super.onResume()
        presenter.v2pQueryLocalRdp()
    }

    override fun p2vQueryLocalRdp(rdpEntities: List<RdpEntity>) {
        presenter.v2pResolveAllGroup(rdpEntities, object : BaseSubscriber<List<String?>?>() {
            fun onError(e: RxException) {
                p2vShowToast(e.getMessage())
            }

            fun onSuccess(groups: List<String?>) {
                binding!!.tlGroup.removeAllTabs()
                if (ObjectUtils.isEmpty(groups)) {
                    binding!!.tlGroup.visibility = View.GONE
                    return
                }
                binding!!.tlGroup.visibility = View.VISIBLE
                for (group in groups) {
                    binding!!.tlGroup.addTab(binding!!.tlGroup.newTab().setText(group))
                }
                p2vShowNormalView()
            }
        })
    }

    override fun p2vQueryLocalRdpByGroup(rdpEntities: List<RdpEntity>, group: String) {
        multiTypeAdapter.items = rdpEntities
        multiTypeAdapter.notifyDataSetChanged()
    }

    override fun p2vGetTabLayout(): TabLayout {
        return binding!!.tlGroup
    }

    override fun p2vClickStatusView() {
        ARouter.getInstance().build(NavigationPath.PATH_RDP_ADD_ACTIVITY).navigation()
    }

}