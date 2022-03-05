package com.xiaoyv.business.global.editor

import com.xiaoyv.blueprint.base.ImplBasePresenter
import com.xiaoyv.blueprint.base.subscribesWithPresenter
import java.io.File

/**
 * FileEditorPresenter
 *
 * @author why
 * @since 2022/3/4
 */
class FileEditorPresenter : ImplBasePresenter<FileEditorContract.View>(),
    FileEditorContract.Presenter {

    private val model = FileEditorModel()

    override fun v2pLoadFileContent(file: File) {
        requireView.p2vShowLoading("正在加载内容")

        model.p2mLoadFileContent(file)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    requireView.p2vHideLoading()
                    requireView.p2vShowFileContent(it)
                },
                onError = {
                    requireView.p2vHideLoading()
                    requireView.p2vShowLoadFileError(it.message.orEmpty())
                })
    }

    override fun v2pSaveFileContent(content: String, filePath: String, finish: Boolean) {
        requireView.p2vShowLoading("保存中...")

        model.p2mSaveFileContent(content, filePath)
            .subscribesWithPresenter(
                presenter = this,
                onSuccess = {
                    requireView.p2vHideLoading()
                    requireView.p2vSaveResult(it, finish)
                },
                onError = {
                    requireView.p2vHideLoading()
                    requireView.p2vSaveResult(false, finish)
                })
    }
}