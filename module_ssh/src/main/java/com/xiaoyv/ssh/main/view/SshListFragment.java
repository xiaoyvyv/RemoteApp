package com.xiaoyv.ssh.main.view;

import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.drakeet.multitype.MultiTypeAdapter;
import com.google.android.material.tabs.TabLayout;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.busines.exception.RxException;
import com.xiaoyv.busines.room.entity.SshEntity;
import com.xiaoyv.ssh.R;
import com.xiaoyv.ssh.add.AddSshActivity;
import com.xiaoyv.ssh.databinding.SshFragmentMainBinding;
import com.xiaoyv.ssh.main.adapter.SshListBinder;
import com.xiaoyv.ssh.main.contract.SshListContract;
import com.xiaoyv.ssh.main.presenter.SshListPresenter;
import com.xiaoyv.ssh.terminal.view.TerminalActivity;
import com.xiaoyv.ui.dialog.OptionsDialog;
import com.xiaoyv.ui.listener.SimpleRefreshListener;
import com.xiaoyv.ui.listener.SimpleTabSelectListener;

import java.util.List;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * SshFragment
 *
 * @author why
 * @since 2020/11/29
 **/
@Route(path = NavigationPath.PATH_SSH_FRAGMENT)
public class SshListFragment extends BaseMvpFragment<SshListContract.View, SshListPresenter> implements SshListContract.View {
    private SshFragmentMainBinding binding;
    private MultiTypeAdapter multiTypeAdapter;
    private SshListBinder sshListBinder;
    private IOverScrollDecor scrollDecor;

    @Override
    protected View createContentView() {
        binding = SshFragmentMainBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected SshListPresenter createPresenter() {
        return new SshListPresenter();
    }

    @Override
    protected void initView() {
        binding.toolbar
                .setTitle(StringUtils.getString(R.string.ssh_main_title))
                .setEndIcon(R.drawable.ui_icon_search)
                .setEndClickListener(v -> {
                    ActivityUtils.startActivity(AddSshActivity.class);
                });

        scrollDecor = OverScrollDecoratorHelper.setUpOverScroll(binding.rvContent, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);

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

    @Override
    protected void initData() {
        sshListBinder = new SshListBinder();
        multiTypeAdapter = new MultiTypeAdapter();
        multiTypeAdapter.register(SshEntity.class, sshListBinder);
        binding.rvContent.setAdapter(multiTypeAdapter);
    }

    @Override
    protected void initListener() {
        sshListBinder.setOnItemChildClickListener((view, dataBean, adapterPos, longClick) -> {
            // 连接
            if (!longClick) {
                TerminalActivity.openSelf(dataBean);
                return;
            }
            OptionsDialog optionsDialog = OptionsDialog.get(activity);
            optionsDialog.setCancelable(true);
            optionsDialog.setOptions(StringUtils.getStringArray(R.array.ui_context_menu));
            optionsDialog.setLastTextColor(ColorUtils.getColor(R.color.ui_status_error));
            optionsDialog.show();
            optionsDialog.setOnItemChildClickListener(position -> {
                switch (position) {
                    // 连接
                    case 0:
                        TerminalActivity.openSelf(dataBean);
                        break;
                    // 编辑
                    case 1:
                        AddSshActivity.openSelf(dataBean);
                        break;
                    // 删除
                    case 2:
                        removeItem(dataBean, adapterPos);
                        break;
                }
            });
        });

        binding.fabAddSsh.setOnClickListener(v ->
                ARouter.getInstance().build(NavigationPath.PATH_SSH_ADD_ACTIVITY).navigation());

        binding.tlGroup.addOnTabSelectedListener(new SimpleTabSelectListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                presenter.v2pQueryLocalSshByGroup(String.valueOf(tab.getText()));
            }
        });

        // 刷新
        scrollDecor.setOverScrollUpdateListener(new SimpleRefreshListener() {
            @Override
            public void onRefresh() {
                TabLayout.Tab tab = binding.tlGroup.getTabAt(binding.tlGroup.getSelectedTabPosition());
                if (tab != null) {
                    String group = String.valueOf(tab.getText());
                    presenter.v2pQueryLocalSshByGroup(group);
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.v2pQueryLocalSsh();
    }

    @Override
    public void removeItem(SshEntity dataBean, int adapterPos) {
        multiTypeAdapter.getItems().remove(adapterPos);
        multiTypeAdapter.notifyItemRemoved(adapterPos);
        // 删除后重新查询
        presenter.v2pDeleteSsh(dataBean, result -> presenter.v2pQueryLocalSsh());
    }

    @Override
    public void p2vQueryLocalSsh(List<SshEntity> SshEntities) {
        presenter.v2pResolveAllGroup(SshEntities, new BaseSubscriber<List<String>>() {
            @Override
            public void onError(RxException e) {
                p2vShowToast(e.getMessage());
            }

            @Override
            public void onSuccess(List<String> groups) {
                binding.tlGroup.removeAllTabs();
                if (ObjectUtils.isEmpty(groups)) {
                    binding.tlGroup.setVisibility(View.GONE);
                    return;
                }
                binding.tlGroup.setVisibility(View.VISIBLE);
                for (String group : groups) {
                    binding.tlGroup.addTab(binding.tlGroup.newTab().setText(group));
                }
                p2vShowNormalView();
            }
        });
    }

    @Override
    public void p2vQueryLocalSshByGroup(List<SshEntity> SshEntities, String group) {
        multiTypeAdapter.setItems(SshEntities);
        multiTypeAdapter.notifyDataSetChanged();
    }

    @Override
    public TabLayout p2vGetTabLayout() {
        return binding.tlGroup;
    }
}

