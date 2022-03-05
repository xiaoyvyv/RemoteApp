package com.xiaoyv.business.global.editor

import com.xiaoyv.blueprint.base.IBaseModel
import com.xiaoyv.blueprint.base.IBasePresenter
import com.xiaoyv.blueprint.base.IBaseView
import io.reactivex.rxjava3.core.Observable
import java.io.File

/**
 * FileEditorContract
 *
 * @author why
 * @since 2022/2/28
 */
interface FileEditorContract {
    interface View : IBaseView {

        fun p2vShowFileContent(content: String)
        fun p2vSaveResult(success: Boolean, finish: Boolean)

        fun p2vShowLoadFileError(errMsg: String)
    }

    interface Presenter : IBasePresenter {
        fun v2pLoadFileContent(file: File)

        /**
         * 保存
         */
        fun v2pSaveFileContent(content: String, filePath: String, finish: Boolean = false)
    }

    interface Model : IBaseModel {
        fun p2mLoadFileContent(file: File): Observable<String>
        fun p2mSaveFileContent(content: String, filePath: String): Observable<Boolean>

    }
}