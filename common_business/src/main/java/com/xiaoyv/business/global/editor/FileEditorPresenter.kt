package com.xiaoyv.business.global.editor

import com.xiaoyv.blueprint.base.ImplBasePresenter

/**
 * FileEditorPresenter
 *
 * @author why
 * @since 2022/3/4
 */
class FileEditorPresenter : ImplBasePresenter<FileEditorContract.View>(),
    FileEditorContract.Presenter {

    private val model = FileEditorModel()

}