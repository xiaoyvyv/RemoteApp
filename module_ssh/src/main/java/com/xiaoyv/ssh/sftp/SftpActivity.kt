package com.xiaoyv.ssh.sftp

import com.blankj.utilcode.constant.MemoryConstants
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.xiaoyv.busines.ftp.BaseFtpActivity
import com.xiaoyv.busines.ftp.BaseFtpFile

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
        val threshold = 100 * MemoryConstants.KB
        val size = dataBean.size
        if (threshold < size) {
            ToastUtils.showShort("暂不支持打开超过 100KB 的文件")
            return
        }

        presenter.v2pDownloadFile(dataBean)
    }


    companion object {
        @JvmStatic
        fun openSelf() {
            ActivityUtils.startActivity(SftpActivity::class.java)
        }
    }
}