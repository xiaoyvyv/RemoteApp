package com.xiaoyv.ssh.main.presenter;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.xiaoyv.busines.base.BaseSubscriber;
import com.xiaoyv.busines.base.ImplBasePresenter;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.busines.exception.RxException;
import com.xiaoyv.busines.room.database.DateBaseManger;
import com.xiaoyv.busines.room.entity.SshEntity;
import com.xiaoyv.ssh.R;
import com.xiaoyv.ssh.main.contract.SshListContract;
import com.xiaoyv.ssh.main.model.SshListModel;
import com.xiaoyv.ui.listener.SimpleResultListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;

/**
 * SshListPresenter
 *
 * @author why
 * @since 2020/11/29
 **/
public class SshListPresenter extends ImplBasePresenter<SshListContract.View> implements SshListContract.Presenter {
    private final SshListContract.Model model;

    public SshListPresenter() {
        this.model = new SshListModel();
    }

    @Override
    public void v2pQueryLocalSsh() {
        model.p2mQueryLocalSsh()
                .compose(bindTransformer())
                .to(bindLifecycle())
                .subscribe(new BaseSubscriber<List<SshEntity>>() {
                    @Override
                    public void onError(RxException e) {
                        // 没有配置的桌面则显示去创建
                        getView().p2vGetStatusView()
                                .showTryAgain(StringUtils.getString(R.string.ssh_main_list_empty), StringUtils.getString(R.string.ssh_main_add_now), v ->
                                        ARouter.getInstance().build(NavigationPath.PATH_SSH_ADD_ACTIVITY).navigation());
                        getView().p2vGetTabLayout().removeAllTabs();
                    }

                    @Override
                    public void onSuccess(List<SshEntity> rdpEntities) {
                        if (ObjectUtils.isEmpty(rdpEntities)) {
                            onError(null);
                            return;
                        }
                        getView().p2vQueryLocalSsh(rdpEntities);
                    }
                });
    }

    @Override
    public void v2pQueryLocalSshByGroup(String group) {
        model.p2mQueryLocalSshByGroup(group)
                .compose(bindTransformer())
                .to(bindLifecycle())
                .subscribe(new BaseSubscriber<List<SshEntity>>() {
                    @Override
                    public void onError(RxException e) {
                        // 当前组没有配置的桌面则显示暂无内容
                        getView().p2vShowEmptyView();
                    }

                    @Override
                    public void onSuccess(List<SshEntity> rdpEntities) {
                        if (ObjectUtils.isEmpty(rdpEntities)) {
                            getView().p2vShowEmptyView();
                            return;
                        }
                        getView().p2vQueryLocalSshByGroup(rdpEntities, group);
                    }
                });
    }

    @Override
    public void v2pResolveAllGroup(@NonNull List<SshEntity> rdpEntities, BaseSubscriber<List<String>> subscribe) {
        Observable.create((ObservableOnSubscribe<List<String>>) emitter -> {
            List<String> groups = new ArrayList<>();
            for (SshEntity rdpEntity : rdpEntities) {
                String group = rdpEntity.group;
                if (StringUtils.isEmpty(group)) {
                    group = StringUtils.getString(R.string.ssh_main_group);
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
    public void v2pDeleteSsh(@NonNull final SshEntity dataBean, @NonNull final SimpleResultListener<Boolean> resultListener) {
        // 删除配置信息
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
            @Override
            public Boolean doInBackground() {
                // 清除 SshEntity 当前条目
                DateBaseManger.get().getSshDao().delete(dataBean);
                return true;
            }

            @Override
            public void onSuccess(Boolean result) {
                resultListener.onResult(result);
            }
        });
    }
}
