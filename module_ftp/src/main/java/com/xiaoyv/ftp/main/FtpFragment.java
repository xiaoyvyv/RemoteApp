package com.xiaoyv.ftp.main;

import android.view.View;

import com.alibaba.android.arouter.facade.annotation.Route;
import com.xiaoyv.blueprint.base.BaseFragment;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.desktop.ftp.databinding.FtpFragmentMainBinding;

/**
 * FtpFragment
 *
 * @author why
 * @since 2020/11/29
 **/
@Route(path = NavigationPath.PATH_FTP_FRAGMENT)
public class FtpFragment extends BaseFragment {

    private FtpFragmentMainBinding binding;

    @Override
    protected View createContentView() {
        binding = FtpFragmentMainBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {
    }

}
