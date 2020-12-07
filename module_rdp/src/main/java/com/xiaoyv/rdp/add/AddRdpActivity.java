


package com.xiaoyv.rdp.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.freerdp.freerdpcore.application.RdpApp;
import com.freerdp.freerdpcore.domain.BaseRdpBookmark;
import com.freerdp.freerdpcore.domain.RdpBookmark;
import com.freerdp.freerdpcore.services.ManualBookmarkGateway;
import com.xiaoyv.busines.base.BaseActivity;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.busines.room.database.DateBaseManger;
import com.xiaoyv.busines.room.entity.RdpEntity;
import com.xiaoyv.rdp.R;
import com.xiaoyv.rdp.databinding.RdpActivityAddBinding;

/**
 * AddRdpActivity
 *
 * @author why
 * @since 2020/11/29
 **/
@Route(path = NavigationPath.PATH_RDP_ADD_ACTIVITY)
public class AddRdpActivity extends BaseActivity {
    public static final String KEY_RDP_ENTITY = "BOOK_MARK";
    private RdpActivityAddBinding binding;
    private String label;
    private String group;
    private String ip;
    private String port;
    private String account;
    private String password;
    private RdpEntity rdpEntity;
    private RdpBookmark bookmark;

    /**
     * 编辑
     *
     * @param rdpEntity 实体
     */
    public static void openSelf(RdpEntity rdpEntity) {
        Intent intent = new Intent(Utils.getApp(), AddRdpActivity.class);
        intent.putExtra(KEY_RDP_ENTITY, rdpEntity);
        ActivityUtils.startActivity(intent);
    }

    @Override
    protected View createContentView() {
        binding = RdpActivityAddBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initIntentData(Intent intent, Bundle bundle) {
        rdpEntity = (RdpEntity) getIntent().getSerializableExtra(KEY_RDP_ENTITY);
        if (rdpEntity == null) {
            rdpEntity = new RdpEntity();
            rdpEntity.label = "远程桌面";
            rdpEntity.group = StringUtils.getString(R.string.rdp_add_group_default);
            rdpEntity.port = StringUtils.getString(R.string.rdp_add_port_default);
        }
        if (StringUtils.isEmpty(rdpEntity.bookmark)) {
            bookmark = new RdpBookmark();
        } else {
            bookmark = GsonUtils.fromJson(rdpEntity.bookmark, RdpBookmark.class);
        }
    }

    @Override
    protected void initView() {
        binding.asvLabel.setTitle(StringUtils.getString(R.string.rdp_add_label))
                .setHint(StringUtils.getString(R.string.rdp_add_label_hint))
                .setMessage(rdpEntity.label)
                .setRequiredText(getString(R.string.rdp_add_label_required));
        binding.asvGroup.setTitle(StringUtils.getString(R.string.rdp_add_group))
                .setHint(StringUtils.getString(R.string.rdp_add_group_hint))
                .setMessage(rdpEntity.group)
                .setRequiredText(null);
        binding.asvIp.setTitle(StringUtils.getString(R.string.rdp_add_ip))
                .setHint(getString(R.string.rdp_add_ip_hint))
                .setMessage(rdpEntity.ip)
                .setRequiredText(getString(R.string.rdp_add_ip_required));

        binding.asvPort.setTitle(StringUtils.getString(R.string.rdp_add_port))
                .setInputNumberType(5)
                .setHint(StringUtils.getString(R.string.rdp_add_port_hint))
                .setMessage(rdpEntity.port)
                .setRequiredText(null);
        binding.asvAccount.setTitle(StringUtils.getString(R.string.rdp_add_account))
                .setHint(StringUtils.getString(R.string.rdp_add_account_hint))
                .setMessage(rdpEntity.account)
                .setRequiredText(getString(R.string.rdp_add_account_required));
        binding.asvPassword.setTitle(StringUtils.getString(R.string.rdp_add_password))
                .setHint(StringUtils.getString(R.string.rdp_add_password_hint))
                .setMessage(rdpEntity.password);
    }

    @Override
    protected void initData() {
        binding.toolbar.setTitle(getString(R.string.rdp_add_title))
                .setStartClickListener(v -> onBackPressed())
                .setEndIcon(R.drawable.ui_icon_save)
                .setEndClickListener(v -> {
                    label = binding.asvLabel.getMessage();
                    group = binding.asvGroup.getMessage();
                    ip = binding.asvIp.getMessage();
                    port = binding.asvPort.getMessage();
                    account = binding.asvAccount.getMessage();
                    password = binding.asvPassword.getMessage();

                    if (StringUtils.isEmpty(label)) {
                        p2vShowToast(StringUtils.getString(R.string.rdp_add_label_empty));
                        return;
                    }
                    if (StringUtils.isEmpty(group)) {
                        p2vShowToast(StringUtils.getString(R.string.rdp_add_group_empty));
                        return;
                    }
                    if (StringUtils.isEmpty(ip)) {
                        p2vShowToast(StringUtils.getString(R.string.rdp_add_ip_empty));
                        return;
                    }
                    if (StringUtils.isEmpty(port)) {
                        p2vShowToast(StringUtils.getString(R.string.rdp_add_port_empty));
                        return;
                    }
                    if (StringUtils.isEmpty(account)) {
                        p2vShowToast(StringUtils.getString(R.string.rdp_add_account_empty));
                        return;
                    }

                    port = StringUtils.isEmpty(port) ? StringUtils.getString(R.string.rdp_add_port_default) : port;
                    group = StringUtils.isEmpty(group) ? StringUtils.getString(R.string.rdp_add_group_default) : group;

                    RdpEntity rdpEntity = new RdpEntity();
                    rdpEntity.label = label;
                    rdpEntity.group = group;
                    rdpEntity.ip = ip;
                    rdpEntity.port = port;
                    rdpEntity.account = account;
                    rdpEntity.password = password;
                    rdpEntity.domain = ip;

                    saveRdpBookmark(rdpEntity);
                });
    }

    /**
     * 保存书签
     *
     * @param rdpEntity 配置的连接信息
     */
    private void saveRdpBookmark(RdpEntity rdpEntity) {
        // 保存配置信息
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
            @Override
            public Boolean doInBackground() throws NumberFormatException {
                bookmark.setHostname(rdpEntity.ip);
                bookmark.setPort(Integer.parseInt(rdpEntity.port));
                bookmark.setLabel(rdpEntity.label);
                bookmark.setUsername(rdpEntity.account);
                bookmark.setPassword(rdpEntity.password);
                bookmark.setDomain(rdpEntity.domain);

                ManualBookmarkGateway bookmarkGateway = RdpApp.getManualBookmarkGateway();
                if (bookmark.getType() == BaseRdpBookmark.TYPE_MANUAL) {
                    // 删除此书签的任何历史记录条目
                    RdpApp.getQuickConnectHistoryGateway().removeHistoryItem(bookmark.getHostname());
                }

                // 插入或更新书签
                if (bookmark.getId() > 0) {
                    bookmarkGateway.update(bookmark);
                } else {
                    bookmarkGateway.insert(bookmark);
                }

                rdpEntity.bookmark = GsonUtils.toJson(bookmark);

                DateBaseManger.get().saveRdp(rdpEntity);
                return true;
            }

            @Override
            public void onSuccess(Boolean result) {
                onBackPressed();
            }
        });
    }
}
