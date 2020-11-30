package com.xiaoyv.ui.listener;

import com.google.android.material.tabs.TabLayout;

/**
 * SampleTabSelectListener
 *
 * @author why
 * @since 2020/11/29
 **/
public abstract class SimpleTabSelectListener implements TabLayout.OnTabSelectedListener {

    @Override
    public abstract void onTabSelected(TabLayout.Tab tab);

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }
}
