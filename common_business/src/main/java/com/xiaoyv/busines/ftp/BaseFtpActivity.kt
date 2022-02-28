package com.xiaoyv.busines.ftp

import android.view.LayoutInflater
import androidx.annotation.CallSuper
import com.xiaoyv.blueprint.base.binding.BaseMvpBindingActivity
import com.xiaoyv.desktop.business.databinding.BusinessActivityFtpBinding
import com.xiaoyv.widget.binder.setOnItemClickListener

/**
 * BaseFtpActivity
 *
 * @author why
 * @since 2022/2/28
 */
abstract class BaseFtpActivity<V : BaseFtpContract.View, P : BaseFtpPresenter<V>> :
    BaseMvpBindingActivity<BusinessActivityFtpBinding, V, P>(), BaseFtpContract.View {

    private lateinit var fileBinder: BaseFtpBinder

    override fun createContentBinding(layoutInflater: LayoutInflater): BusinessActivityFtpBinding {
        return BusinessActivityFtpBinding.inflate(layoutInflater)
    }

    @CallSuper
    override fun initView() {
        binding.rvList.canLoadMore = false

    }

    @CallSuper
    override fun initData() {
        fileBinder = BaseFtpBinder()
        binding.rvList.addItemBinder(fileBinder)

        binding.rvList.showLoadingView()
    }

    @CallSuper
    override fun initListener() {
        binding.rvList.setDateListener(
            onRefreshListener = {
                // 查询当前目录
                presenter.v2pQueryFileList()
            }
        )

        /**
         * 重试
         */
        binding.rvList.onRetryListener = { _, _ ->
            // 查询当前目录
            presenter.v2pQueryFileList()
        }

        fileBinder.setOnItemClickListener { view, dataBean, position, isLongClick ->
            val fileName = dataBean.fileName
            if (fileName.isBlank()) {
                return@setOnItemClickListener
            }
            // 打开目录
            if (dataBean.isDirOrDirLink) {
                presenter.v2pQueryFileList(fileName)
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
        presenter.v2pQueryFileList()
    }

    override fun p2vUpdatePathBar(pwdPath: String) {

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
            presenter.v2pQueryFileList("..")
        } else {
            super.onBackPressed()
        }
    }

}