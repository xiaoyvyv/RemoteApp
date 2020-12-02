package com.xiaoyv.rdp.screen.presenter;

import com.xiaoyv.busines.base.ImplBasePresenter;
import com.xiaoyv.rdp.screen.contract.ScreenContract;
import com.xiaoyv.rdp.screen.model.ScreenModel;

/**
 * Presenter
 *
 * @author why
 * @since 2020/12/02
 **/
public class ScreenPresenter extends ImplBasePresenter<ScreenContract.View> {
    private final ScreenContract.Model model;

    public ScreenPresenter() {
        this.model = new ScreenModel();
    }


}
