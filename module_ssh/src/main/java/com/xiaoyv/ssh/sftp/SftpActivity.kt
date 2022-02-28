package com.xiaoyv.ssh.sftp

import com.blankj.utilcode.util.ActivityUtils
import com.xiaoyv.busines.ftp.BaseFtpActivity

/**
 * SftpActivity
 *
 * @author why
 * @since 2022/2/28
 */
class SftpActivity : BaseFtpActivity<SftpContract.View, SftpPresenter>(), SftpContract.View {

    override fun createPresenter() = SftpPresenter()

    override fun initData() {
        super.initData()
    }

    companion object {
        @JvmStatic
        fun openSelf() {
            ActivityUtils.startActivity(SftpActivity::class.java)
        }
    }
}