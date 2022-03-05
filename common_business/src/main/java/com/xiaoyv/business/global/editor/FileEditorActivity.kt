package com.xiaoyv.business.global.editor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.constant.MemoryConstants
import com.blankj.utilcode.util.FileUtils
import com.xiaoyv.blueprint.base.binding.BaseMvpBindingActivity
import com.xiaoyv.blueprint.base.rxjava.event.RxEvent
import com.xiaoyv.blueprint.rxbus.RxBus
import com.xiaoyv.business.config.EditorType
import com.xiaoyv.business.config.NavigationKey
import com.xiaoyv.business.config.NavigationPath
import com.xiaoyv.business.rx.RxEventTag
import com.xiaoyv.business.utils.showDialog
import com.xiaoyv.desktop.business.databinding.BusinessActivityFileEditorBinding
import com.xiaoyv.widget.utils.spi
import io.github.rosemoe.sora.text.Content
import io.github.rosemoe.sora.text.ContentListener
import java.io.File

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

    private var editorType: Int = EditorType.TYPE_NONE
    private var filePath: String = ""
    private var isEdit = false

    override fun createContentBinding(layoutInflater: LayoutInflater): BusinessActivityFileEditorBinding {
        return BusinessActivityFileEditorBinding.inflate(layoutInflater)
    }

    override fun initIntentData(intent: Intent, bundle: Bundle, isNewIntent: Boolean) {
        editorType = intent.getIntExtra(NavigationKey.KEY_INT, editorType)
        filePath = intent.getStringExtra(NavigationKey.KEY_STRING) ?: filePath
    }

    override fun createPresenter() = FileEditorPresenter()

    override fun initView() {
        binding.toolbar.title = FileUtils.getFileName(filePath)
        binding.toolbar.bottomDivider = false

        binding.ceEditor.setTextSize(10f)
        binding.ceEditor.setScaleTextSizes(8.spi.toFloat(), 24.spi.toFloat())
        binding.ceEditor.isCursorAnimationEnabled = false
    }

    override fun initData() {

    }

    override fun initListener() {
        binding.ceEditor.text.addContentListener(object : ContentListener {
            override fun beforeReplace(content: Content?) {
                isEdit = true
            }

            override fun afterInsert(
                content: Content?,
                startLine: Int,
                startColumn: Int,
                endLine: Int,
                endColumn: Int,
                insertedContent: CharSequence?
            ) {
                isEdit = true
            }

            override fun afterDelete(
                content: Content?,
                startLine: Int,
                startColumn: Int,
                endLine: Int,
                endColumn: Int,
                deletedContent: CharSequence?
            ) {
                isEdit = true
            }
        })
    }

    override fun onPresenterCreated() {
        val file = File(filePath)
        if (file.exists()) {
            presenter.v2pLoadFileContent(file)
        }
    }

    override fun p2vShowFileContent(content: String) {
        binding.ceEditor.setText(content)
        initListener()
    }

    override fun p2vSaveResult(success: Boolean, finish: Boolean) {
        p2vShowToast(if (success) "保存成功" else "保存失败")

        if (finish) {
            // 保存成功事件
            if (success) {
                RxBus.getDefault().post(RxEvent().apply {
                    dataBoolean = isEdit
                    dataInt = editorType
                    dataSerializable = filePath
                }, RxEventTag.EVENT_EDITOR_SAVE_SUCCESS)
            }

            isEdit = false

            // 结束
            finish()

            return
        }

        isEdit = false
    }

    override fun p2vShowLoadFileError(errMsg: String) {
        p2vShowToast(errMsg)

        finish()
    }

    override fun onBackPressed() {
        if (isEdit) {
            showDialog(
                content = "是否保存该文件？",
                onConfirmListener = {
                    it.dismissAllowingStateLoss()
                    presenter.v2pSaveFileContent(
                        binding.ceEditor.text.toStringBuilder().toString(),
                        filePath, true
                    )
                },
                onCancelListener = {
                    it.dismissAllowingStateLoss()
                    finish()
                }
            )
        } else {
            super.onBackPressed()
        }
    }

}
