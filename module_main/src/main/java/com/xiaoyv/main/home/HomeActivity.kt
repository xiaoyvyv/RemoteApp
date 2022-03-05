package com.xiaoyv.main.home

import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.alibaba.android.arouter.launcher.ARouter
import com.xiaoyv.blueprint.base.binding.BaseBindingActivity
import com.xiaoyv.blueprint.utils.LazyUtils.loadFragments
import com.xiaoyv.blueprint.utils.LazyUtils.showHideFragment
import com.xiaoyv.business.config.NavigationPath
import com.xiaoyv.desktop.main.R
import com.xiaoyv.desktop.main.databinding.MainActivityHomeBinding

/**
 * HomeActivity
 *
 * @author why
 * @since 2020/12/12
 */
class HomeActivity : BaseBindingActivity<MainActivityHomeBinding>() {
    private lateinit var rdpFragment: Fragment
    private lateinit var sshFragment: Fragment
    private lateinit var ftpFragment: Fragment
    private lateinit var mineFragment: Fragment

    private var currentShowIndex = 0

    override fun createContentBinding(layoutInflater: LayoutInflater): MainActivityHomeBinding {
        return MainActivityHomeBinding.inflate(layoutInflater)
    }

    override fun initView() {

    }

    override fun initData() {
        rdpFragment =
            ARouter.getInstance().build(NavigationPath.PATH_RDP_FRAGMENT).navigation() as Fragment
        sshFragment =
            ARouter.getInstance().build(NavigationPath.PATH_SSH_FRAGMENT).navigation() as Fragment
        ftpFragment =
            ARouter.getInstance().build(NavigationPath.PATH_FTP_FRAGMENT).navigation() as Fragment
        mineFragment =
            ARouter.getInstance().build(NavigationPath.PATH_MINE_FRAGMENT).navigation() as Fragment

        loadFragments(
            binding.flContainer.id,
            currentShowIndex,
            rdpFragment,
            sshFragment,
            ftpFragment,
            mineFragment
        )
    }

    override fun initListener() {

        binding.bnvTab.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.main_home_rdp -> {
                    showHideFragment(rdpFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.main_home_ssh -> {
                    showHideFragment(sshFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.main_home_ftp -> {
                    showHideFragment(ftpFragment)
                    return@setOnItemSelectedListener true
                }
                R.id.main_home_mine -> {
                    showHideFragment(mineFragment)
                    return@setOnItemSelectedListener true
                }
            }
            true
        }
    }
}