package com.xiaoyv.ssh.terminal.presenter;

import com.xiaoyv.busines.base.ImplBasePresenter;
import com.xiaoyv.ssh.terminal.contract.TerminalContract;
import com.xiaoyv.ssh.terminal.model.TerminalModel;

/**
 * TerminalPresenter
 *
 * @author why
 * @since 2020/12/06
 **/
public class TerminalPresenter extends ImplBasePresenter<TerminalContract.View> implements TerminalContract.Presenter {
    private TerminalContract.Model model;

    public TerminalPresenter() {
        this.model = new TerminalModel();
    }


}
