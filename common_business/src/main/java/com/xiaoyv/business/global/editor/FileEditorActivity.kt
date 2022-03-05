package com.xiaoyv.business.global.editor

import android.view.LayoutInflater
import com.alibaba.android.arouter.facade.annotation.Route
import com.xiaoyv.blueprint.base.binding.BaseMvpBindingActivity
import com.xiaoyv.business.config.NavigationPath
import com.xiaoyv.desktop.business.databinding.BusinessActivityFileEditorBinding

/**
 * FileEditorActivity
 *
 * @author why
 * @since 2022/3/4
 */
@Route(path = NavigationPath.PATH_ACTIVITY_EDITOR)
class FileEditorActivity :
    BaseMvpBindingActivity<BusinessActivityFileEditorBinding, FileEditorContract.View, FileEditorPresenter>(),
    FileEditorContract.View {
    override fun createContentBinding(layoutInflater: LayoutInflater): BusinessActivityFileEditorBinding {
        return BusinessActivityFileEditorBinding.inflate(layoutInflater)
    }

    override fun createPresenter() = FileEditorPresenter()

    override fun initView() {

    }

    override fun initData() {

    }
}