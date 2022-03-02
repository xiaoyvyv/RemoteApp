package com.xiaoyv.ssh.sftp

import android.view.View
import com.blankj.utilcode.constant.MemoryConstants
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.xiaoyv.busines.ftp.BaseFtpActivity
import com.xiaoyv.busines.ftp.BaseFtpFile
import com.xiaoyv.widget.dialog.UiNormalDialog

/**
 * SftpActivity
 *
 * @author why
 * @since 2022/2/28
 */
class SftpActivity : BaseFtpActivity<SftpContract.View, SftpPresenter>(), SftpContract.View {

    override fun createPresenter() = SftpPresenter()


    override fun vClickSymLink(dataBean: BaseFtpFile, position: Int) {
        presenter.v2pDownloadFile(dataBean)
    }

    override fun vClickFile(dataBean: BaseFtpFile, position: Int) {
        val threshold = 50 * MemoryConstants.MB
        val size = dataBean.size
        if (threshold < size) {
            ToastUtils.showShort("暂不支持打开超过 50MB 的文件")
            return
        }

        UiNormalDialog.Builder()
            .apply {
                message = "正在下载中，请稍等"
                touchOutsideCancelable = false
                confirmCancelable = false
                onCancelClickListener = { _, _ ->
                    presenter.v2pCancelDownloadFile(dataBean)
                }
                onConfirmClickListener = { _, _ ->
                    presenter.v2pDownloadFile(dataBean)
                }
            }
            .create()
            .show(this)
    }


    companion object {
        @JvmStatic
        fun openSelf() {
            ActivityUtils.startActivity(SftpActivity::class.java)
        }
    }
}