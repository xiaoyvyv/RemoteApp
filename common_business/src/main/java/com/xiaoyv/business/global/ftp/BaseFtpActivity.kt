package com.xiaoyv.business.global.ftp

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.SpanUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.xiaoyv.blueprint.base.binding.BaseMvpBindingActivity
import com.xiaoyv.blueprint.base.rxjava.event.RxEvent
import com.xiaoyv.business.config.EditorType
import com.xiaoyv.business.config.NavigationKey
import com.xiaoyv.business.nav.NavHelper
import com.xiaoyv.business.rx.RxEventTag
import com.xiaoyv.business.utils.showDialog
import com.xiaoyv.desktop.business.R
import com.xiaoyv.desktop.business.databinding.BusinessActivityFtpBinding
import com.xiaoyv.desktop.business.databinding.BusinessActivityFtpDownloadBinding
import com.xiaoyv.desktop.business.databinding.BusinessActivityFtpStatBinding
import com.xiaoyv.desktop.ui.databinding.UiDialogBinding
import com.xiaoyv.widget.binder.setOnItemClickListener
import com.xiaoyv.widget.dialog.UiNormalDialog
import com.xiaoyv.widget.utils.doOnBarClick

/**
 * BaseFtpActivity
 *
 * @author why
 * @since 2022/2/28
 */
abstract class BaseFtpActivity<V : BaseFtpContract.View, P : BaseFtpPresenter<V>> :
    BaseMvpBindingActivity<BusinessActivityFtpBinding, V, P>(), BaseFtpContract.View {

    private lateinit var fileBinder: BaseFtpBinder
    private lateinit var filePathBinder: BaseFtpPathBinder
    private lateinit var filePathAdapter: BaseBinderAdapter

    private var downloadDialog: UiNormalDialog? = null

    private var title = "SFTP | FTP"

    override fun createContentBinding(layoutInflater: LayoutInflater): BusinessActivityFtpBinding {
        return BusinessActivityFtpBinding.inflate(layoutInflater)
    }

    @CallSuper
    override fun initIntentData(intent: Intent, bundle: Bundle, isNewIntent: Boolean) {
        title = intent.getStringExtra(NavigationKey.KEY_STRING) ?: title
    }

    @CallSuper
    override fun initView() {
        binding.rvList.canLoadMore = false

        binding.toolbar.title = title
        binding.toolbar.setLeftIcon(
            R.drawable.ui_icon_nav_back,
            onBarClickListener = doOnBarClick { _, which ->
                when (which) {
                    0 -> finish()
                }
            })
    }

    @CallSuper
    override fun initData() {
        fileBinder = BaseFtpBinder()
        binding.rvList.addItemBinder(fileBinder)
        binding.rvList.showLoadingView()

        filePathBinder = BaseFtpPathBinder()
        filePathAdapter = BaseBinderAdapter()
        filePathAdapter.addItemBinder(filePathBinder)
        binding.rvPath.adapter = filePathAdapter


        addReceiveEventTag(RxEventTag.EVENT_EDITOR_SAVE_SUCCESS)
    }

    @CallSuper
    override fun initListener() {
        binding.rvList.setDateListener(
            onRefreshListener = {
                // 查询当前目录
                presenter.v2pQueryFileList(showLoading = false)
            }
        )

        /**
         * 重试
         */
        binding.rvList.onRetryListener = { _, _ ->
            // 查询当前目录
            presenter.v2pQueryFileList(showLoading = false)
        }

        fileBinder.setOnItemClickListener { view, dataBean, position, isLongClick ->
            val fileName = dataBean.fileName
            if (fileName.isBlank()) {
                return@setOnItemClickListener
            }

            // 长按
            if (isLongClick) {
                processItemLongClick(dataBean, position)
                return@setOnItemClickListener
            }
            // 点击
            else {
                processItemClick(dataBean, position)
            }
        }

        // 上方路径点击监听
        filePathBinder.setOnItemClickListener { _, _, position, _ ->
            val pathListSize = filePathAdapter.data.size

            // 点击位置到当前目录的层级数目
            val clickIndexToLastStep = pathListSize - position - 1
            val backPathArray = arrayListOf<String>().apply {
                repeat(clickIndexToLastStep) {
                    add("..")
                }
            }
            val backPath = backPathArray.joinToString("/")
            presenter.v2pQueryFileList(backPath)
        }
    }

    override fun onReceiveRxEvent(rxEvent: RxEvent, rxEventTag: String) {
        when (rxEventTag) {
            RxEventTag.EVENT_EDITOR_SAVE_SUCCESS -> {
                val editorType = rxEvent.dataInt
                val isEdit = rxEvent.dataBoolean
                val filePath = rxEvent.dataSerializable.toString()

                if (isEdit.not()) {
                    return
                }

                when (editorType) {
                    EditorType.TYPE_SFTP -> {
                        showDialog(content = "是否上传该更改后的文件？", onConfirmListener = {
                            it.dismissAllowingStateLoss()

                            presenter.v2pUploadFile(filePath, "")
                        })
                    }
                    EditorType.TYPE_FTP -> {

                    }
                }
            }
        }
    }

    override fun processItemClick(baseFtpFile: BaseFtpFile, position: Int) {
        val fileName = baseFtpFile.fileName

        when {
            // 文件：打开目录
            baseFtpFile.isDirectory -> {
                presenter.v2pQueryFileList(fileName, showLoading = true)
            }
            // 链接：则解析
            baseFtpFile.isSymlink -> {
                vClickSymLink(baseFtpFile, position)
            }
            // 文件
            else -> {
                vClickFile(baseFtpFile, position)
            }
        }
    }

    abstract fun vClickSymLink(baseFtpFile: BaseFtpFile, position: Int)
    abstract fun vClickFile(baseFtpFile: BaseFtpFile, position: Int)

    protected open fun processItemLongClick(baseFtpFile: BaseFtpFile, position: Int) {
        val fileName = baseFtpFile.fileName
        presenter.v2pQueryFileStat(fileName)
    }

    @SuppressLint("SetTextI18n")
    fun vStartDownload(baseFtpFile: BaseFtpFile) {
        UiNormalDialog.Builder()
            .apply {
                cancelText = null
                confirmText = null
                customView = R.layout.ui_dialog
                onCustomViewInitListener = { dialog, view ->
                    val dialogBinding = UiDialogBinding.bind(view)
                    dialogBinding.tvMsg.text = String.format("是否下载该文件：%s ?", baseFtpFile.fileName)
                    dialogBinding.tvDone.setOnClickListener {
                        dialog.dismissAllowingStateLoss()
                        // 下载中对话框
                        vShowDownloadingDialog(baseFtpFile)

                        // 下载中
                        presenter.v2pDownloadFile(baseFtpFile)
                    }
                    dialogBinding.tvTemp.setOnClickListener {
                        dialog.dismissAllowingStateLoss()
                    }
                }
            }
            .create()
            .show(this)
    }

    private fun vShowDownloadingDialog(baseFtpFile: BaseFtpFile) {
        downloadDialog = UiNormalDialog.Builder()
            .apply {
                cancelText = null
                confirmText = null
                customView = R.layout.business_activity_ftp_download
                touchOutsideCancelable = false
                backCancelable = false
                onCustomViewInitListener = { dialog, view ->
                    val downloadBinding = BusinessActivityFtpDownloadBinding.bind(view)

                    downloadBinding.tvMsg.text = String.format("正在下载文件：%s", baseFtpFile.fileName)
                    downloadBinding.tvTemp.setOnClickListener {
                        dialog.dismissAllowingStateLoss()
                        presenter.v2pCancelDownloadFile(baseFtpFile)
                    }
                }
            }.create()

        downloadDialog?.show(this)
    }

    override fun p2vShowDownloadError(errMsg: String) {
        downloadDialog?.dismissAllowingStateLoss()
    }

    override fun p2vShowDownloadProgress(downloadFile: BaseFtpDownloadFile) {
        val view = downloadDialog?.requireCustomView ?: return
        val downloadBinding = BusinessActivityFtpDownloadBinding.bind(view)
        val current = ConvertUtils.byte2FitMemorySize(downloadFile.current, 2)
        val total = ConvertUtils.byte2FitMemorySize(downloadFile.total, 2)
        val downloadSpeed = ConvertUtils.byte2FitMemorySize(downloadFile.downloadSpeed, 2)

        downloadBinding.pbProgress.progress = (downloadFile.progress * 100).toInt()
        downloadBinding.tvSpeed.text = String.format("%s | %s %s/s", total, current, downloadSpeed)

        if (downloadFile.finish) {
            downloadBinding.tvMsg.text = "下载完成"
            downloadBinding.pbProgress.isGone = true
            downloadBinding.tvSpeed.isGone = true
            downloadBinding.tvDone.isVisible = true

            downloadBinding.tvTemp.setOnClickListener {
                downloadDialog?.dismissAllowingStateLoss()
            }
            downloadBinding.tvDone.text = "打开"
            downloadBinding.tvDone.setOnClickListener {
                downloadDialog?.dismissAllowingStateLoss()

                val downloadFilePath = downloadFile.downloadFilePath.orEmpty()
                if (downloadFilePath.isNotBlank()) {
                    NavHelper.jumpFileEditorActivity(downloadFilePath, EditorType.TYPE_SFTP)
                }
            }
        }
    }

    @CallSuper
    override fun onPresenterCreated() {
        // 查询当前目录
        presenter.v2pQueryPwdPath()
    }

    override fun p2vShowPwdPath(pwdPath: String) {
        // 设置初始工作路径
        presenter.v2pUpdatePwdPath(pwdPath)

        // 查询目录内容
        presenter.v2pQueryFileList(showLoading = false)
    }

    override fun p2vUpdatePathBar(pwdPath: List<String>) {
        filePathAdapter.setList(pwdPath)
    }

    /**
     * 文件属性
     */
    override fun p2vShowFileStat(ftpStat: BaseFtpStat) {
        UiNormalDialog.Builder().apply {
            cancelText = null
            confirmText = null
            customView = R.layout.business_activity_ftp_stat
            onCustomViewInitListener = { _, view ->
                val binding = BusinessActivityFtpStatBinding.bind(view)

                val memorySize = ConvertUtils.byte2FitMemorySize(ftpStat.fileSize, 2)

                val acTime = TimeUtils.getFriendlyTimeSpanByNow(ftpStat.fileAcTime * 1000)
                val moTime = TimeUtils.getFriendlyTimeSpanByNow(ftpStat.fileMoTime * 1000)

                val fileUser = "${ftpStat.fileUserId}/${ftpStat.fileUser}"
                val fileGroup = "${ftpStat.fileGroupId}/${ftpStat.fileGroup}"

                SpanUtils.with(binding.tvStat)
                    .appendLine(String.format("名称　　　　：%s", ftpStat.fileName))
                    .appendLine(String.format("类型　　　　：%s", ftpStat.fileType))
                    .apply {
                        if (ftpStat.isSymlink) {
                            appendLine(String.format("链接目标　　：%s", ftpStat.linkTargetPath))
                        }
                    }
                    .appendLine(String.format("大小　　　　：%s", memorySize))
                    .appendLine(String.format("权限　　　　：%s", ftpStat.filePermission))
                    .appendLine(String.format("所属用户　　：%s", fileUser))
                    .appendLine(String.format("所属组　　　：%s", fileGroup))
                    .appendLine(String.format("访问时间　　：%s", acTime))
                    .appendLine(String.format("修改时间　　：%s", moTime))
                    .appendLine(String.format("Inode　　　：%s", ftpStat.inode))
                    .appendLine(String.format("Block　　　：%s", ftpStat.block))
                    .appendLine(String.format("Io Block  ：%s", ftpStat.ioBlock))
                    .create()

            }
        }.create().show(this)
    }

    override fun p2vShowFileListSuccess(fileList: List<BaseFtpFile>) {
        binding.rvList.finishAll()
        binding.rvList.setList(fileList)

        if (fileList.isNullOrEmpty()) {
            binding.rvList.showErrorView("该目录没有文件")
        } else {
            binding.rvList.showContentView()
        }
    }

    override fun p2vShowFileListError(errMsg: String) {
        binding.rvList.finishAll()
        binding.rvList.showErrorView(errMsg)
    }

    override fun onBackPressed() {
        if (presenter.v2pCanBack()) {
            // 查询上一条
            presenter.v2pQueryFileList("..", showLoading = true)
        } else {
            super.onBackPressed()
        }
    }

}