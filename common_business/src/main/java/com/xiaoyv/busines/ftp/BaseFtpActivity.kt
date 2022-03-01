package com.xiaoyv.busines.ftp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.SpanUtils
import com.blankj.utilcode.util.TimeUtils
import com.chad.library.adapter.base.BaseBinderAdapter
import com.xiaoyv.blueprint.base.binding.BaseMvpBindingActivity
import com.xiaoyv.busines.config.NavigationKey
import com.xiaoyv.desktop.business.R
import com.xiaoyv.desktop.business.databinding.BusinessActivityFtpBinding
import com.xiaoyv.desktop.business.databinding.BusinessActivityFtpStatBinding
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

    private var title = "SFTP | FTP"

    override fun createContentBinding(layoutInflater: LayoutInflater): BusinessActivityFtpBinding {
        return BusinessActivityFtpBinding.inflate(layoutInflater)
    }

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


    override fun processItemClick(dataBean: BaseFtpFile, position: Int) {
        val fileName = dataBean.fileName

        when {
            // 文件：打开目录
            dataBean.isDirectory -> {
                presenter.v2pQueryFileList(fileName, showLoading = true)
            }
            // 链接：则解析
            dataBean.isSymlink -> {
                vClickSymLink(dataBean,position)
            }
            // 文件
            else -> {
                vClickFile(dataBean,position)
            }
        }
    }

    abstract fun vClickSymLink(dataBean: BaseFtpFile, position: Int)
    abstract fun vClickFile(dataBean: BaseFtpFile, position: Int)

    protected open fun processItemLongClick(dataBean: BaseFtpFile, position: Int) {
        val fileName = dataBean.fileName
        presenter.v2pQueryFileStat(fileName)
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

                val memorySize = ConvertUtils.byte2FitMemorySize(ftpStat.fileSize)

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