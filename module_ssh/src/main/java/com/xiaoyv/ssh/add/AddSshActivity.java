package com.xiaoyv.ssh.add;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
import com.blankj.utilcode.util.Utils;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.busines.room.database.DateBaseManger;
import com.xiaoyv.busines.room.entity.SshEntity;
import com.xiaoyv.ssh.R;
import com.xiaoyv.ssh.databinding.SshAddActivitiyBinding;

/**
 * AddSshActivity
 *
 * @author why
 * @since 2020/12/07
 **/
@Route(path = NavigationPath.PATH_SSH_ADD_ACTIVITY)
public class AddSshActivity extends BaseActivity {
    public static final String KEY_SSH_ENTITY = "KEY_SSH_ENTITY";
    private SshAddActivitiyBinding binding;
    private String label;
    private String group;
    private String ip;
    private String port;
    private String account;
    private String password;
    private SshEntity sshEntity;

    /**
     * 编辑
     *
     * @param sshEntity 实体
     */
    public static void openSelf(SshEntity sshEntity) {
        Intent intent = new Intent(Utils.getApp(), AddSshActivity.class);
        intent.putExtra(KEY_SSH_ENTITY, sshEntity);
        ActivityUtils.startActivity(intent);
    }

    @Override
    protected View createContentView() {
        binding = SshAddActivitiyBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initIntentData(@NonNull Intent intent, @NonNull Bundle bundle) {
        sshEntity = (SshEntity) getIntent().getSerializableExtra(KEY_SSH_ENTITY);
        if (sshEntity == null) {
            sshEntity = new SshEntity();
            sshEntity.label = "远程SSH";
            sshEntity.group = StringUtils.getString(R.string.ssh_add_group_default);
            sshEntity.port = StringUtils.getString(R.string.ssh_add_port_default);
        }
    }

    @Override
    protected void initView() {
        binding.asvLabel.setTitle(StringUtils.getString(R.string.ssh_add_label))
                .setHint(StringUtils.getString(R.string.ssh_add_label_hint))
                .setMessage(sshEntity.label)
                .setMessageHint(getString(R.string.ssh_add_label_required));
        binding.asvGroup.setTitle(StringUtils.getString(R.string.ssh_add_group))
                .setHint(StringUtils.getString(R.string.ssh_add_group_hint))
                .setMessage(sshEntity.group)
                .setMessageHint(null);
        binding.asvIp.setTitle(StringUtils.getString(R.string.ssh_add_ip))
                .setHint(getString(R.string.ssh_add_ip_hint))
                .setMessage(sshEntity.ip)
                .setMessageHint(getString(R.string.ssh_add_ip_required));

        binding.asvPort.setTitle(StringUtils.getString(R.string.ssh_add_port))
                .setInputNumberType(5)
                .setHint(StringUtils.getString(R.string.ssh_add_port_hint))
                .setMessage(sshEntity.port)
                .setMessageHint(null);
        binding.asvAccount.setTitle(StringUtils.getString(R.string.ssh_add_account))
                .setHint(StringUtils.getString(R.string.ssh_add_account_hint))
                .setMessage(sshEntity.account)
                .setMessageHint(getString(R.string.ssh_add_account_required));
        binding.asvPassword.setTitle(StringUtils.getString(R.string.ssh_add_password))
                .setHint(StringUtils.getString(R.string.ssh_add_password_hint))
                .setMessage(sshEntity.password);

    }

    @Override
    protected void initData() {
        binding.toolbar.setTitle(getString(R.string.ssh_add_title))
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
                        p2vShowToast(StringUtils.getString(R.string.ssh_add_label_empty));
                        return;
                    }
                    if (StringUtils.isEmpty(group)) {
                        p2vShowToast(StringUtils.getString(R.string.ssh_add_group_empty));
                        return;
                    }
                    if (StringUtils.isEmpty(ip)) {
                        p2vShowToast(StringUtils.getString(R.string.ssh_add_ip_empty));
                        return;
                    }
                    if (StringUtils.isEmpty(port)) {
                        p2vShowToast(StringUtils.getString(R.string.ssh_add_port_empty));
                        return;
                    }
                    if (StringUtils.isEmpty(account)) {
                        p2vShowToast(StringUtils.getString(R.string.ssh_add_account_empty));
                        return;
                    }

                    port = StringUtils.isEmpty(port) ? StringUtils.getString(R.string.ssh_add_port_default) : port;
                    group = StringUtils.isEmpty(group) ? StringUtils.getString(R.string.ssh_add_group_default) : group;

                    SshEntity sshEntity = new SshEntity();
                    sshEntity.label = label;
                    sshEntity.group = group;
                    sshEntity.ip = ip;
                    sshEntity.port = port;
                    sshEntity.account = account;
                    sshEntity.password = password;
                    sshEntity.domain = ip;

                    saveSshBookmark(sshEntity);
                });
    }

    /**
     * 保存书签
     *
     * @param sshEntity 配置的连接信息
     */
    private void saveSshBookmark(SshEntity sshEntity) {
        // 保存配置信息
        ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
            @Override
            public Boolean doInBackground() throws NumberFormatException {
                DateBaseManger.get().saveSsh(sshEntity);
                return true;
            }

            @Override
            public void onSuccess(Boolean result) {
                onBackPressed();
            }
        });
    }
}
