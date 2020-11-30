package com.xiaoyv.ssh.main;

import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.xiaoyv.busines.base.BaseFragment;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.ssh.databinding.SshFragmentMainBinding;

/**
 * SshFragment
 *
 * @author why
 * @since 2020/11/29
 **/
@Route(path = NavigationPath.PATH_SSH_FRAGMENT)
public class SshFragment extends BaseFragment {
    private SshFragmentMainBinding binding;

    @Override
    protected View createContentView() {
        binding = SshFragmentMainBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }
}

