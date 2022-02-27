package com.xiaoyv.ssh.main

import android.view.LayoutInflater
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.blankj.utilcode.util.ColorUtils
import com.blankj.utilcode.util.ObjectUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.github.nukc.stateview.StateView
import com.google.android.material.tabs.TabLayout
import com.xiaoyv.blueprint.base.binding.BaseMvpBindingFragment
import com.xiaoyv.blueprint.base.rxjava.event.RxEvent
import com.xiaoyv.busines.config.NavigationPath
import com.xiaoyv.busines.room.entity.SshEntity
import com.xiaoyv.busines.rx.RxEventTag
import com.xiaoyv.desktop.ssh.R
import com.xiaoyv.desktop.ssh.databinding.SshFragmentMainBinding
import com.xiaoyv.ssh.add.AddSshActivity
import com.xiaoyv.ssh.terminal.TerminalActivity
import com.xiaoyv.ui.listener.SimpleRefreshListener
import com.xiaoyv.ui.listener.SimpleTabSelectListener
import com.xiaoyv.widget.binder.setOnItemClickListener
import com.xiaoyv.widget.dialog.UiOptionsDialog
import com.xiaoyv.widget.toolbar.UiToolbar
import com.xiaoyv.widget.utils.overScrollV

/**
 * SshFragment
 *
 * @author why
 * @since 2020/11/29
 */
@Route(path = NavigationPath.PATH_SSH_FRAGMENT)
class SshListFragment :
    BaseMvpBindingFragment<SshFragmentMainBinding, SshListContract.View, SshListPresenter>(),
    SshListContract.View {

    private lateinit var multiTypeAdapter: BaseBinderAdapter
    private lateinit var sshListBinder: SshListBindingBinder

    private var currentGroupIndex = 0

    override fun createContentBinding(layoutInflater: LayoutInflater): SshFragmentMainBinding {
        return SshFragmentMainBinding.inflate(layoutInflater)
    }

    override fun createPresenter() = SshListPresenter()

    override fun initView() {
        stateController.fitTitleAndStatusBar = true
        binding.toolbar.title = StringUtils.getString(R.string.ssh_main_title)
        binding.toolbar.setRightIcon(R.drawable.ui_icon_search, onBarClickListener = object :
            UiToolbar.OnBarClickListener {
            override fun onClick(view: View, which: Int) {
                ARouter.getInstance().build(NavigationPath.PATH_SSH_ADD_ACTIVITY).navigation()
//                ActivityUtils.startActivity(AddSshActivity::class.java)
            }
        })

        binding.rvContent.overScrollV()

//
//        ThreadUtils.getCachedPool().execute(() -> {
//            try {
//                Connection conn = new Connection("101.132.108.0");
//                conn.connect();
//                boolean authenticate = conn.authenticateWithPassword("root", "Why981229@");
//                if (!authenticate) {
//                    LogUtils.e("身份验证失败,请重试");
//                    return;
//                }
//                Session session = conn.openSession();
//                InputStream stdout = session.getStdout();
//                OutputStream stdin = session.getStdin();
//                session.requestDumbPTY();
//                session.startShell();
//
//                TermSession termSession = new TermSession();
//                termSession.setTermIn(stdout);
//                termSession.setTermOut(stdin);
//                termSession.setColorScheme(new ColorScheme(Color.WHITE, Color.BLACK, Color.WHITE, Color.BLACK));
//                termSession.setDefaultUTF8Mode(true);
//                termSession.setFinishCallback(session1 -> LogUtils.json(session1));
//                termSession.setTitle("");
//                termSession.initializeEmulator(80, 24);
//                termSession.setUpdateCallback(() -> LogUtils.e("onUpdate"));
//
//                UI.post(() -> {
//                    DisplayMetrics displayMetrics = new DisplayMetrics();
//                    activity.getDisplay().getRealMetrics(displayMetrics);
//
//                    binding.evTerminal.attachSession(termSession);
//                    binding.evTerminal.setDensity(displayMetrics);
//                    binding.evTerminal.setTextSize(16);
//                    binding.evTerminal.setUseCookedIME(true);
//                    binding.evTerminal.setBackKeyCharacter(32);
//                    binding.evTerminal.setAltSendsEsc(false);
//                    binding.evTerminal.setControlKeyCode(0);
//                    binding.evTerminal.setFnKeyCode(1);
//                    binding.evTerminal.setMouseTracking(false);
//                    binding.evTerminal.setOnKeyListener(new View.OnKeyListener() {
//                        @Override
//                        public boolean onKey(View v, int keyCode, KeyEvent event) {
//                            return false;
//                        }
//                    });
//                    binding.evTerminal.setExtGestureListener(new EmulatorViewGestureListener(binding.evTerminal));
//                    registerForContextMenu(binding.evTerminal);
//                });
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        });
    }

    override fun initData() {
        sshListBinder = SshListBindingBinder()
        multiTypeAdapter = BaseBinderAdapter()
        multiTypeAdapter.addItemBinder(sshListBinder)
        binding.rvContent.adapter = multiTypeAdapter

        addReceiveEventTag(RxEventTag.EVENT_ADD_SSH_SUCCESS)
    }

    override fun initFinish() {
        presenter.v2pQueryGroup()
    }

    override fun initListener() {
        sshListBinder.setOnItemClickListener { _, dataBean, position, isLongClick ->
            // 连接
            if (!isLongClick) {
                p2vShowLoading("正在校验主机连通性")
                presenter.v2pCheckHost(dataBean)
                return@setOnItemClickListener
            }

            val optionsDialog = UiOptionsDialog.Builder().apply {
                backCancelable = true
                itemDataList = StringUtils.getStringArray(R.array.ui_context_menu).toList()
                itemLastColor = ColorUtils.getColor(R.color.ui_status_error)

                onOptionsClickListener = { dialog, _, index ->
                    dialog.dismiss()
                    when (index) {
                        0 -> {
                            p2vShowLoading("正在校验主机连通性")
                            presenter.v2pCheckHost(dataBean)
                        }
                        1 -> AddSshActivity.openSelf(dataBean)
                        2 -> removeItem(dataBean, position)
                    }
                    true
                }
            }.create()
            optionsDialog.show(this)
        }

        binding.fabAdd.setOnClickListener {
            ARouter.getInstance().build(NavigationPath.PATH_SSH_ADD_ACTIVITY).navigation()
        }

        binding.tlGroup.addOnTabSelectedListener(object : SimpleTabSelectListener() {
            override fun onTabSelected(tab: TabLayout.Tab) {
                currentGroupIndex = tab.position
                presenter.v2pQueryLocalSshByGroup(tab.text.toString())
            }
        })

        // 刷新
        binding.rvContent.overScrollV()
            .setOverScrollUpdateListener(object : SimpleRefreshListener() {
                override fun onRefresh() {
                    val tab = binding.tlGroup.getTabAt(currentGroupIndex) ?: return
                    val group = tab.text.toString()
                    presenter.v2pQueryLocalSshByGroup(group)
                }
            })
    }

    override fun p2vCheckHostResult(sshEntity: SshEntity, success: Boolean) {
        p2vHideLoading()
        if (success) {
            TerminalActivity.openSelf(sshEntity)
        } else {
            ToastUtils.showShort("%s:%s 连通性校验失败", sshEntity.ip, sshEntity.port)
        }
    }

    override fun onReceiveRxEvent(rxEvent: RxEvent, rxEventTag: String) {
        when (rxEventTag) {
            RxEventTag.EVENT_ADD_SSH_SUCCESS -> {
                // 更新查询
                presenter.v2pQueryGroup()
            }
        }
    }

    override fun removeItem(dataBean: SshEntity, adapterPos: Int) {
        multiTypeAdapter.data.remove(adapterPos)
        multiTypeAdapter.notifyItemRemoved(adapterPos)

        // 删除
        presenter.v2pDeleteSsh(dataBean)
    }

    override fun p2vShowSshGroups(groupNames: List<String>) {
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

    override fun p2vShowSshListByGroup(sshEntities: List<SshEntity>, group: String) {
        multiTypeAdapter.setList(sshEntities)
        multiTypeAdapter.notifyItemRangeChanged(0, sshEntities.size)
    }

    override fun p2vDeleteSshResult(success: Boolean) {
        // 删除后重新查询
        presenter.v2pQueryGroup()
    }

    override fun p2vGetTabLayout() = binding.tlGroup

    override fun p2vClickStatusView(stateView: StateView, view: View) {
        ARouter.getInstance().build(NavigationPath.PATH_SSH_ADD_ACTIVITY).navigation()
    }
}