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

    /**
     * 当前目录
     */
    protected var currentDirName = "/home"

    /**
     * 操作历史
     */
    protected val dirHistory = arrayListOf<String>()

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

    }

    @CallSuper
    override fun initListener() {
        binding.rvList.setDateListener(
            onRefreshListener = {
                // 查询当前目录
                presenter.v2pQueryFileList(currentDirName)
            }
        )

        binding.rvList.onRetryListener = { _, _ ->
            // 查询当前目录
            presenter.v2pQueryFileList(currentDirName)
        }

        fileBinder.setOnItemClickListener { view, dataBean, position, isLongClick ->
            val fileName = dataBean.fileName
            if (fileName.isBlank()) {
                return@setOnItemClickListener
            }
            // 打开目录
            if (dataBean.isDir) {
                currentDirName = "$currentDirName/$fileName"
                presenter.v2pQueryFileList(currentDirName)
            }
        }
    }

    @CallSuper
    override fun onPresenterCreated() {
        // 查询当前目录
        presenter.v2pQueryFileList(currentDirName)
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
}