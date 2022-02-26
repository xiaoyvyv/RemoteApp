package com.xiaoyv.main.home;

import android.view.View;

import androidx.fragment.app.Fragment;

import com.alibaba.android.arouter.launcher.ARouter;
import com.blankj.utilcode.util.FragmentUtils;
import com.xiaoyv.blueprint.base.BaseActivity;
import com.xiaoyv.busines.config.NavigationPath;
import com.xiaoyv.desktop.main.R;
import com.xiaoyv.desktop.main.databinding.MainActivityHomeBinding;

/**
 * HomeActivity
 *
 * @author why
 * @since 2020/12/12
 */
public class HomeActivity extends BaseActivity {
    private MainActivityHomeBinding binding;
    private Fragment rdpFragment;
    private Fragment sshFragment;
    private Fragment ftpFragment;
    private Fragment mineFragment;

    @Override
    protected View createContentView() {
        binding = MainActivityHomeBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {
        rdpFragment = (Fragment) ARouter.getInstance().build(NavigationPath.PATH_RDP_FRAGMENT).navigation();
        sshFragment = (Fragment) ARouter.getInstance().build(NavigationPath.PATH_SSH_FRAGMENT).navigation();
        ftpFragment = (Fragment) ARouter.getInstance().build(NavigationPath.PATH_FTP_FRAGMENT).navigation();
        mineFragment = (Fragment) ARouter.getInstance().build(NavigationPath.PATH_MINE_FRAGMENT).navigation();


        FragmentUtils.add(getSupportFragmentManager(), rdpFragment, binding.flContainer.getId(), false);
        FragmentUtils.add(getSupportFragmentManager(), sshFragment, binding.flContainer.getId(), true);
        FragmentUtils.add(getSupportFragmentManager(), ftpFragment, binding.flContainer.getId(), true);
        FragmentUtils.add(getSupportFragmentManager(), mineFragment, binding.flContainer.getId(), true);
    }

    @Override
    protected void initData() {
        binding.bnvTab.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.main_home_rdp) {
                FragmentUtils.showHide(rdpFragment, sshFragment, ftpFragment, mineFragment);
                return true;
            }
            if (item.getItemId() == R.id.main_home_ssh) {
                FragmentUtils.showHide(sshFragment, rdpFragment, ftpFragment, mineFragment);
                return true;
            }
            if (item.getItemId() == R.id.main_home_ftp) {
                FragmentUtils.showHide(ftpFragment, sshFragment, rdpFragment, mineFragment);
                return true;
            }
            if (item.getItemId() == R.id.main_home_mine) {
                FragmentUtils.showHide(mineFragment, ftpFragment, sshFragment, rdpFragment);
                return true;
            }
            return false;
        });
    }
}