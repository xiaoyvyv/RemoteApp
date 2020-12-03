package com.xiaoyv.rdp.main.view;

import android.util.Log;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.drakeet.multitype.MultiTypeAdapter;
import com.freerdp.freerdpcore.application.RdpApp;
import com.freerdp.freerdpcore.domain.BaseRdpBookmark;
import com.freerdp.freerdpcore.presentation.BookmarkActivity;
import com.google.android.material.tabs.TabLayout;
import com.xiaoyv.busines.base.BaseMvpFragment;
import com.xiaoyv.busines.base.BaseSubscriber;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.busines.exception.RxException;
import com.xiaoyv.busines.room.database.DateBaseManger;
import com.xiaoyv.busines.room.entity.RdpEntity;
import com.xiaoyv.busines.utils.ScanUtils;
import com.xiaoyv.rdp.R;
import com.xiaoyv.rdp.add.AddRdpActivity;
import com.xiaoyv.rdp.databinding.RdpFragmentMainBinding;
import com.xiaoyv.rdp.main.adapter.RdpListBinder;
import com.xiaoyv.rdp.main.contract.RdpListContract;
import com.xiaoyv.rdp.main.presenter.RdpListPresenter;
import com.xiaoyv.ui.dialog.OptionsDialog;
import com.xiaoyv.ui.listener.SimpleRefreshListener;
import com.xiaoyv.ui.listener.SimpleTabSelectListener;

import java.util.ArrayList;
import java.util.List;

import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper;

/**
 * RdpFragment
 *
 * @author why
 * @since 2020/11/29
 **/
@Route(path = NavigationPath.PATH_RDP_FRAGMENT)
public class RdpListFragment extends BaseMvpFragment<RdpListContract.View, RdpListPresenter> implements RdpListContract.View {
    private RdpFragmentMainBinding binding;
    private MultiTypeAdapter multiTypeAdapter;
    private RdpListBinder rdpBinder;
    private IOverScrollDecor scrollDecor;

    @Override
    protected View createContentView() {
        binding = RdpFragmentMainBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected RdpListPresenter createPresenter() {
        return new RdpListPresenter();
    }

    @Override
    protected void initView() {
        binding.toolbar
                .setTitle(StringUtils.getString(R.string.rdp_main_title))
                .setEndIcon(R.drawable.ui_icon_search)
                .setEndClickListener(v -> {
                    p2vShowToast("搜索遍历");
                });

        scrollDecor = OverScrollDecoratorHelper.setUpOverScroll(binding.rvContent, OverScrollDecoratorHelper.ORIENTATION_VERTICAL);
    }

    @Override
    protected void initData() {
        rdpBinder = new RdpListBinder();
        multiTypeAdapter = new MultiTypeAdapter();
        multiTypeAdapter.register(RdpEntity.class, rdpBinder);
        binding.rvContent.setAdapter(multiTypeAdapter);

    }


    @Override
    protected void initListener() {
        rdpBinder.setOnItemChildClickListener((view, dataBean, adapterPos, longClick) -> {
            // 连接
            if (!longClick) {
                ThreadUtils.executeByFixed(10, new ScanUtils.ScanIpTask());
                return;
            }
            OptionsDialog optionsDialog = OptionsDialog.get(activity);
            optionsDialog.setCancelable(true);
            optionsDialog.setOptions(StringUtils.getStringArray(R.array.rdp_context_menu));
            optionsDialog.setLastTextColor(ColorUtils.getColor(R.color.ui_status_error));
            optionsDialog.show();
            optionsDialog.setOnItemChildClickListener(position -> {
                // 连接
                if (position == 0) {
                    return;
                }
                // 编辑
                if (position == 1) {
                }
                // 删除
                if (position == 2) {
                    multiTypeAdapter.getItems().remove(adapterPos);
                    multiTypeAdapter.notifyItemRemoved(adapterPos);
                    multiTypeAdapter.notifyItemRangeChanged(adapterPos, multiTypeAdapter.getItemCount());
                    // 保存配置信息
                    ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
                        @Override
                        public Boolean doInBackground() {
                            DateBaseManger.get().getRdpDao().delete(dataBean);
                            return true;
                        }

                        @Override
                        public void onSuccess(Boolean result) {
                            presenter.v2pQueryLocalRdp();
                        }
                    });
                }
            });
        });

        binding.fabAdd.setOnClickListener(v -> {
            ARouter.getInstance().build(NavigationPath.PATH_RDO_ADD_ACTIVITY).navigation();
        });

        binding.tlGroup.addOnTabSelectedListener(new SimpleTabSelectListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                presenter.v2pQueryLocalRdpByGroup(String.valueOf(tab.getText()));
            }
        });

        // 刷新
        scrollDecor.setOverScrollUpdateListener(new SimpleRefreshListener() {
            @Override
            public void onRefresh() {
                TabLayout.Tab tab = binding.tlGroup.getTabAt(binding.tlGroup.getSelectedTabPosition());
                if (tab != null) {
                    String group = String.valueOf(tab.getText());
                    presenter.v2pQueryLocalRdpByGroup(group);
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        presenter.v2pQueryLocalRdp();
    }

    @Override
    public void p2vQueryLocalRdp(List<RdpEntity> rdpEntities) {
        presenter.resolveAllGroup(rdpEntities, new BaseSubscriber<List<String>>() {
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
            }
        });
    }

    @Override
    public void p2vQueryLocalRdpByGroup(List<RdpEntity> rdpEntities, String group) {
        multiTypeAdapter.setItems(rdpEntities);
        multiTypeAdapter.notifyDataSetChanged();
    }

}