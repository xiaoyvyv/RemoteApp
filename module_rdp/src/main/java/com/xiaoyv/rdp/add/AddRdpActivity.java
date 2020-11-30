package com.xiaoyv.rdp.add;

import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ThreadUtils;
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
                .setHint(StringUtils.getString(R.string.rdp_add_group_hint));
        binding.asvIp.setTitle(StringUtils.getString(R.string.rdp_add_ip))
                .setHint(getString(R.string.rdp_add_ip_hint));
        binding.asvPort.setTitle(StringUtils.getString(R.string.rdp_add_port))
                .setInputTypeNumber(5)
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
                    String label = binding.asvLabel.getMessage();
                    String group = binding.asvGroup.getMessage();
                    String ip = binding.asvIp.getMessage();
                    String port = binding.asvPort.getMessage();
                    String account = binding.asvAccount.getMessage();
                    String password = binding.asvPassword.getMessage();
                    // 保存配置信息
                    ThreadUtils.executeByCached(new ThreadUtils.SimpleTask<Boolean>() {
                        @Override
                        public Boolean doInBackground() {
                            RdpEntity rdpEntity = new RdpEntity();
                            rdpEntity.label = label;
                            rdpEntity.group = group;
                            rdpEntity.ip = ip;
                            rdpEntity.port = port;
                            rdpEntity.account = account;
                            rdpEntity.password = password;
                            rdpEntity.domain = ip;
                            DateBaseManger.get().getRdpDao().insert(rdpEntity);
                            return true;
                        }

                        @Override
                        public void onSuccess(Boolean result) {
                            p2vShowToast(StringUtils.getString(R.string.rdp_add_success));
                            onBackPressed();
                        }
                    });
                });

    }
}
