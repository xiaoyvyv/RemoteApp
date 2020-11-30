package com.xiaoyv.rdp.main.view;

import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.drakeet.multitype.MultiTypeAdapter;
import com.google.android.material.tabs.TabLayout;
import com.xiaoyv.busines.base.BaseMvpFragment;
import com.xiaoyv.busines.base.BaseSubscriber;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.busines.exception.RxException;
import com.xiaoyv.busines.room.database.DateBaseManger;
import com.xiaoyv.busines.room.entity.RdpEntity;
import com.xiaoyv.rdp.R;
import com.xiaoyv.rdp.databinding.RdpFragmentMainBinding;
import com.xiaoyv.rdp.main.adapter.RdpListBinder;
import com.xiaoyv.rdp.main.contract.RdpListContract;
import com.xiaoyv.rdp.main.presenter.RdpListPresenter;
import com.xiaoyv.ui.listener.SimpleRefreshListener;
import com.xiaoyv.ui.listener.SimpleTabSelectListener;

import java.util.List;
import java.util.Random;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import me.everything.android.ui.overscroll.IOverScrollDecor;
import me.everything.android.ui.overscroll.IOverScrollState;
import me.everything.android.ui.overscroll.ListenerStubs;
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
        presenter.v2pQueryLocalRdp();
    }

    @Override
    protected void initListener() {
        binding.fabAdd.setOnClickListener(v -> {
            ARouter.getInstance().build(NavigationPath.PATH_RDO_ADD_ACTIVITY).navigation();
//
//            Observable.create(emitter -> {
//                RdpEntity rdpEntity = new RdpEntity();
//                rdpEntity.group = "Linux";
//                rdpEntity.ip = "192.168.31.0" + new Random().nextInt(255);
//                DateBaseManger.get().getRdpDao().insert(rdpEntity);
//            }).observeOn(AndroidSchedulers.mainThread())
//                    .subscribeOn(Schedulers.io())
//                    .to(bindLifecycle())
//                    .subscribe(o -> {
//                        presenter.v2pQueryLocalRdp();
//                    });
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
                String group = String.valueOf(binding.tlGroup.getTabAt(binding.tlGroup.getSelectedTabPosition()).getText());
                presenter.v2pQueryLocalRdpByGroup(group);
            }
        });

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
