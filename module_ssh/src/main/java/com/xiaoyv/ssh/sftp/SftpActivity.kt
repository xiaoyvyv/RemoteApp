package com.xiaoyv.ssh.sftp

import android.content.Intent
import android.os.Bundle
import com.blankj.utilcode.constant.MemoryConstants
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.ToastUtils
import com.xiaoyv.busines.config.NavigationKey
import com.xiaoyv.busines.ftp.BaseFtpActivity
import com.xiaoyv.busines.ftp.BaseFtpFile
import com.xiaoyv.busines.room.entity.SshEntity
import java.io.Serializable

/**
 * SftpActivity
 *
 * @author why
 * @since 2022/2/28
 */
class SftpActivity : BaseFtpActivity<SftpContract.View, SftpPresenter>(), SftpContract.View {

    private var sshEntity: SshEntity = SshEntity()

    override fun createPresenter() = SftpPresenter()

    override fun initIntentData(intent: Intent, bundle: Bundle, isNewIntent: Boolean) {
        super.initIntentData(intent, bundle, isNewIntent)

        val serializable = intent.getSerializableExtra(NavigationKey.KEY_SERIALIZABLE)
        if (serializable is SshEntity) {
            this.sshEntity = serializable
        }
    }

    override fun onPresenterCreated() {
        // 优化初始化 Connection 配置
        presenter.v2pInitSshEntity(sshEntity)
        super.onPresenterCreated()
    }

    override fun vClickSymLink(baseFtpFile: BaseFtpFile, position: Int) {
        presenter.v2pDownloadFile(baseFtpFile)
    }

    override fun vClickFile(baseFtpFile: BaseFtpFile, position: Int) {
        val threshold = 50 * MemoryConstants.MB
        val size = baseFtpFile.size
        if (threshold < size) {
            ToastUtils.showShort("暂不支持打开超过 50MB 的文件")
            return
        }

        // 准备下载
        vStartDownload(baseFtpFile)
    }


    companion object {

        @JvmStatic
        fun openSelf(serializable: Serializable) {
            ActivityUtils.startActivity(Bundle().apply {
                putSerializable(NavigationKey.KEY_SERIALIZABLE, serializable)
            }, SftpActivity::class.java)
        }
    }
}