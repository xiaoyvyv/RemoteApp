package com.xiaoyv.rdp.main.view;

import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.ArrayUtils;
import com.blankj.utilcode.util.ColorUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ResourceUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.drakeet.multitype.MultiTypeAdapter;
import com.google.android.material.tabs.TabLayout;
import com.xiaoyv.busines.base.BaseItemBinder;
import com.xiaoyv.busines.base.BaseMvpFragment;
import com.xiaoyv.busines.base.BaseSubscriber;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.busines.exception.RxException;
import com.xiaoyv.busines.room.entity.RdpEntity;
import com.xiaoyv.rdp.R;
import com.xiaoyv.rdp.databinding.RdpFragmentMainBinding;
import com.xiaoyv.rdp.main.adapter.RdpListBinder;
import com.xiaoyv.rdp.main.contract.RdpListContract;
import com.xiaoyv.rdp.main.presenter.RdpListPresenter;
import com.xiaoyv.ui.dialog.OptionsDialog;
import com.xiaoyv.ui.listener.SimpleRefreshListener;
import com.xiaoyv.ui.listener.SimpleTabSelectListener;

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
        rdpBinder.setOnItemChildClickListener((view, dataBean, position) -> {
            OptionsDialog optionsDialog = OptionsDialog.get(activity);
            optionsDialog.setOptions(StringUtils.getStringArray(R.array.rdp_context_menu));
            optionsDialog.setLastTextColor(ColorUtils.getColor(R.color.ui_text_c0));
            optionsDialog.show();
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
