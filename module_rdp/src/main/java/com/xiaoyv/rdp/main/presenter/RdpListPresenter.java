package com.xiaoyv.rdp.main.presenter;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.freerdp.freerdpcore.domain.RdpConfig;
import com.xiaoyv.busines.base.BaseSubscriber;
import com.xiaoyv.busines.base.ImplBasePresenter;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.busines.exception.RxException;
import com.xiaoyv.busines.room.database.DateBaseManger;
import com.xiaoyv.busines.room.entity.RdpEntity;
import com.xiaoyv.rdp.R;
import com.xiaoyv.rdp.main.contract.RdpListContract;
import com.xiaoyv.rdp.main.model.RdpListModel;
import com.xiaoyv.ui.listener.SimpleResultListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;

/**
 * RdpListPresenter
 *
 * @author why
 * @since 2020/11/29
 **/
public class RdpListPresenter extends ImplBasePresenter<RdpListContract.View> implements RdpListContract.Presenter {
    private final RdpListContract.Model model;

    public RdpListPresenter() {
        this.model = new RdpListModel();
    }

    @Override
    public void v2pQueryLocalRdp() {
        model.p2mQueryLocalRdp()
                .compose(bindTransformer())
                .to(bindLifecycle())
                .subscribe(new BaseSubscriber<List<RdpEntity>>() {
                    @Override
                    public void onError(RxException e) {
                        // 没有配置的桌面则显示去创建
                        getView().p2vGetStatusView()
                                .showTryAgain(StringUtils.getString(R.string.rdp_main_list_empty), StringUtils.getString(R.string.rdp_main_add_now), v ->
                                                ARouter.getInstance().build(NavigationPath.PATH_RDP_ADD_ACTIVITY).navigation());
                        getView().p2vGetTabLayout().removeAllTabs();
                    }

                    @Override
                    public void onSuccess(List<RdpEntity> rdpEntities) {
                        if (ObjectUtils.isEmpty(rdpEntities)) {
                            onError(null);
                            return;
                        }
                        getView().p2vQueryLocalRdp(rdpEntities);
                    }
                });
    }

    @Override
    public void v2pQueryLocalRdpByGroup(String group) {
        model.p2mQueryLocalRdpByGroup(group)
                .compose(bindTransformer())
                .to(bindLifecycle())
                .subscribe(new BaseSubscriber<List<RdpEntity>>() {
                    @Override
                    public void onError(RxException e) {
                        // 当前组没有配置的桌面则显示暂无内容
                        getView().p2vShowEmptyView();
                    }

                    @Override
                    public void onSuccess(List<RdpEntity> rdpEntities) {
                        if (ObjectUtils.isEmpty(rdpEntities)) {
                            getView().p2vShowEmptyView();
                            return;
                        }
                        getView().p2vQueryLocalRdpByGroup(rdpEntities, group);
                    }
                });
    }

    @Override
    public void v2pResolveAllGroup(@NonNull List<RdpEntity> rdpEntities, BaseSubscriber<List<String>> subscribe) {
        Observable.create((ObservableOnSubscribe<List<String>>) emitter -> {
            List<String> groups = new ArrayList<>();
            for (RdpEntity rdpEntity : rdpEntities) {
                String group = rdpEntity.group;
                if (StringUtils.isEmpty(group)) {
                    group = StringUtils.getString(R.string.rdp_main_group);
                }
                if (!groups.contains(group)) {
                    groups.add(group);
                }
            }
            Collections.sort(groups, String::compareTo);
            emitter.onNext(groups);
            emitter.onComplete();
        }).compose(bindTransformer()).to(bindLifecycle()).subscribe(subscribe);
    }

    @Override
    public void v2pDeleteRdp(@NonNull final RdpEntity dataBean, @NonNull final SimpleResultListener<Boolean> resultListener) {
        // 删除配置信息
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
            @Override
            public Boolean doInBackground() {
                // 清除RDP配置数据库当前条目
                RdpConfig rdpBookmark = GsonUtils.fromJson(dataBean.configStr, RdpConfig.class);

                // 清除 RdpEntity 当前条目
                DateBaseManger.get().getRdpDao().delete(dataBean);
                return true;
            }

            @Override
            public void onSuccess(Boolean result) {
                resultListener.onResult(result);
            }
        });
    }
}
