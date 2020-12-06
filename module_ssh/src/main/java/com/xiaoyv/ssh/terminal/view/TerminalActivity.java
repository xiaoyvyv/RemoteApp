package com.xiaoyv.ssh.terminal.view;

import android.view.View;

import com.xiaoyv.busines.base.BaseMvpActivity;
import com.xiaoyv.ssh.databinding.SshActivityTerminalBinding;
import com.xiaoyv.ssh.terminal.contract.TerminalContract;
import com.xiaoyv.ssh.terminal.presenter.TerminalPresenter;

/**
 * TerminalActivity
 *
 * @author why
 * @since 2020/12/06
 **/
public class TerminalActivity extends BaseMvpActivity<TerminalContract.View, TerminalPresenter> implements TerminalContract.View {
    private SshActivityTerminalBinding binding;

    @Override
    protected TerminalPresenter createPresenter() {
        return new TerminalPresenter();
    }

    @Override
    protected View createContentView() {
        binding = SshActivityTerminalBinding.inflate(getLayoutInflater());
        return binding.getRoot();
    }

    @Override
    protected void initView() {

    }

    @Override
    protected void initData() {

    }
}
