package com.xiaoyv.rdp.add;

import android.content.SharedPreferences;
import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.GsonUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.freerdp.freerdpcore.application.RdpApp;
import com.freerdp.freerdpcore.domain.BaseRdpBookmark;
import com.freerdp.freerdpcore.domain.RdpBookmark;
import com.freerdp.freerdpcore.services.BookmarkBaseGateway;
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
@Route(path = NavigationPath.PATH_RDO_ADD_ACTIVITY)
public class AddRdpActivity extends BaseActivity {
    private RdpActivityAddBinding binding;
    private String label;
    private String group;
    private String ip;
    private String port;
    private String account;
    private String password;

    @Override
    protected View createContentView() {
        binding = RdpActivityAddBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        binding.asvLabel.setTitle(StringUtils.getString(R.string.rdp_add_label))
                .setHint(StringUtils.getString(R.string.rdp_add_label_hint));
        binding.asvGroup.setTitle(StringUtils.getString(R.string.rdp_add_group))
                .setMessage(StringUtils.getString(R.string.rdp_add_group_default))
                .setHint(StringUtils.getString(R.string.rdp_add_group_hint));
        binding.asvIp.setTitle(StringUtils.getString(R.string.rdp_add_ip))
                .setHint(getString(R.string.rdp_add_ip_hint));
        binding.asvPort.setTitle(StringUtils.getString(R.string.rdp_add_port))
                .setMessage(StringUtils.getString(R.string.rdp_add_port_default))
                .setInputNumberType(5)
                .setHint(StringUtils.getString(R.string.rdp_add_port_hint));
        binding.asvAccount.setTitle(StringUtils.getString(R.string.rdp_add_account))
                .setHint(StringUtils.getString(R.string.rdp_add_account_hint));
        binding.asvPassword.setTitle(StringUtils.getString(R.string.rdp_add_password))
                .setHint(StringUtils.getString(R.string.rdp_add_password_hint));

    }

    @Override
    protected void initData() {
        binding.toolbar.setTitle(getString(R.string.rdp_add_title))
                .setStartClickListener(v -> onBackPressed())
                .setEndIcon(R.drawable.rdp_icon_save)
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
                    if (StringUtils.isEmpty(ip)) {
                        p2vShowToast(StringUtils.getString(R.string.rdp_add_ip_empty));
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
                RdpBookmark bookmark = new RdpBookmark();
                bookmark.setHostname(rdpEntity.ip);
                bookmark.setPort(Integer.parseInt(rdpEntity.port));
                bookmark.setLabel(rdpEntity.label);
                bookmark.setUsername(rdpEntity.account);
                bookmark.setPassword(rdpEntity.password);
                bookmark.setDomain(rdpEntity.domain);
                SharedPreferences sp = getSharedPreferences("TEMP", MODE_PRIVATE);
                bookmark.writeToSharedPreferences(sp);

                // 重新取出
                bookmark.readFromSharedPreferences(sp);

                BookmarkBaseGateway bookmarkGateway;
                if (bookmark.getType() == BaseRdpBookmark.TYPE_MANUAL) {
                    bookmarkGateway = RdpApp.getManualBookmarkGateway();
                    // remove any history entry for this
                    // bookmark
                    RdpApp.getQuickConnectHistoryGateway().removeHistoryItem(bookmark.<RdpBookmark>get().getHostname());
                } else {
                    return false;
                }

                // insert or update bookmark and leave
                // activity
                if (bookmark.getId() > 0) {
                    bookmarkGateway.update(bookmark);
                } else {
                    bookmarkGateway.insert(bookmark);
                }

                rdpEntity.bookmark = GsonUtils.toJson(bookmark);

                DateBaseManger.get().getRdpDao().insert(rdpEntity);
                return true;
            }

            @Override
            public void onSuccess(Boolean result) {
                onBackPressed();
            }
        });


    }
}
