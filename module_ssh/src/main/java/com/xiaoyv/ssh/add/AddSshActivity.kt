package com.xiaoyv.ssh.add

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.xiaoyv.blueprint.base.binding.BaseBindingActivity
import com.xiaoyv.blueprint.base.rxjava.subscribes
import com.xiaoyv.busines.config.NavigationPath
import com.xiaoyv.busines.room.database.DateBaseManger
import com.xiaoyv.busines.room.entity.SshEntity
import com.xiaoyv.ssh.R
import com.xiaoyv.ssh.databinding.SshAddActivitiyBinding
import com.xiaoyv.widget.toolbar.UiToolbar
import io.reactivex.rxjava3.core.Observable

/**
 * AddSshActivity
 *
 * @author why
 * @since 2020/12/07
 */
@Route(path = NavigationPath.PATH_SSH_ADD_ACTIVITY)
class AddSshActivity : BaseBindingActivity<SshAddActivitiyBinding>() {
    private var label: String? = null
    private var group: String? = null
    private var ip: String? = null
    private var port: String? = null
    private var account: String? = null
    private var password: String? = null

    private var sshEntity: SshEntity = SshEntity().also {
        it.label = "远程SSH"
        it.group = StringUtils.getString(R.string.ssh_add_group_default)
        it.port = StringUtils.getString(R.string.ssh_add_port_default)
    }

    override fun createContentBinding(layoutInflater: LayoutInflater): SshAddActivitiyBinding {
        return SshAddActivitiyBinding.inflate(layoutInflater)
    }

    override fun initIntentData(intent: Intent, bundle: Bundle, isNewIntent: Boolean) {
        sshEntity = getIntent().getSerializableExtra(KEY_SSH_ENTITY) as? SshEntity ?: sshEntity
    }

    override fun initView() {
        binding.asvLabel.setTitle(StringUtils.getString(R.string.ssh_add_label))
            .setHint(StringUtils.getString(R.string.ssh_add_label_hint))
            .setMessage(sshEntity.label)
            .setMessageHint(getString(R.string.ssh_add_label_required))

        binding.asvGroup.setTitle(StringUtils.getString(R.string.ssh_add_group))
            .setHint(StringUtils.getString(R.string.ssh_add_group_hint))
            .setMessage(sshEntity.group)
            .setMessageHint(null)

        binding.asvIp.setTitle(StringUtils.getString(R.string.ssh_add_ip))
            .setHint(getString(R.string.ssh_add_ip_hint))
            .setMessage(sshEntity.ip)
            .setMessageHint(getString(R.string.ssh_add_ip_required))

        binding.asvPort.setTitle(StringUtils.getString(R.string.ssh_add_port))
            .setInputNumberType(5)
            .setHint(StringUtils.getString(R.string.ssh_add_port_hint))
            .setMessage(sshEntity.port)
            .setMessageHint(null)

        binding.asvAccount.setTitle(StringUtils.getString(R.string.ssh_add_account))
            .setHint(StringUtils.getString(R.string.ssh_add_account_hint))
            .setMessage(sshEntity.account)
            .setMessageHint(getString(R.string.ssh_add_account_required))

        binding.asvPassword.setTitle(StringUtils.getString(R.string.ssh_add_password))
            .setHint(StringUtils.getString(R.string.ssh_add_password_hint))
            .message = sshEntity.password
    }

    override fun initData() {
        binding.toolbar.title = getString(R.string.ssh_add_title)
        binding.toolbar.setRightIcon(
            R.drawable.ui_icon_save,
            onBarClickListener = object : UiToolbar.OnBarClickListener {
                override fun onClick(view: View, which: Int) {
                    save()
                }
            })
    }

    private fun save() {
        label = binding.asvLabel.message
        group = binding.asvGroup.message
        ip = binding.asvIp.message
        port = binding.asvPort.message
        account = binding.asvAccount.message
        password = binding.asvPassword.message

        if (StringUtils.isEmpty(label)) {
            p2vShowToast(StringUtils.getString(R.string.ssh_add_label_empty))
            return
        }
        if (StringUtils.isEmpty(group)) {
            p2vShowToast(StringUtils.getString(R.string.ssh_add_group_empty))
            return
        }
        if (StringUtils.isEmpty(ip)) {
            p2vShowToast(StringUtils.getString(R.string.ssh_add_ip_empty))
            return
        }
        if (StringUtils.isEmpty(port)) {
            p2vShowToast(StringUtils.getString(R.string.ssh_add_port_empty))
            return
        }
        if (StringUtils.isEmpty(account)) {
            p2vShowToast(StringUtils.getString(R.string.ssh_add_account_empty))
            return
        }
        port =
            if (StringUtils.isEmpty(port)) StringUtils.getString(R.string.ssh_add_port_default) else port
        group =
            if (StringUtils.isEmpty(group)) StringUtils.getString(R.string.ssh_add_group_default) else group

        val entity = SshEntity().also {
            it.label = label
            it.group = group
            it.ip = ip
            it.port = port
            it.account = account
            it.password = password
            it.domain = ip
        }

        saveSshBookmark(entity)
    }

    /**
     * 保存书签
     *
     * @param sshEntity 配置的连接信息
     */
    private fun saveSshBookmark(sshEntity: SshEntity) {
        Observable.create<Boolean> {
            DateBaseManger.get().saveSsh(sshEntity)
        }.compose(bindTransformer())
            .to(bindLifecycle())
            .subscribes(
                onSuccess = {
                    onBackPressed()
                },
                onError = {
                    onBackPressed()
                }
            )
    }

    companion object {
        const val KEY_SSH_ENTITY = "KEY_SSH_ENTITY"

        /**
         * 编辑
         *
         * @param sshEntity 实体
         */
        fun openSelf(sshEntity: SshEntity?) {
            val intent = Intent(Utils.getApp(), AddSshActivity::class.java)
            intent.putExtra(KEY_SSH_ENTITY, sshEntity)
            ActivityUtils.startActivity(intent)
        }
    }
}