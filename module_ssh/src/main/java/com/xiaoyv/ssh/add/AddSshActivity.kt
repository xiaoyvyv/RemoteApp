package com.xiaoyv.ssh.add

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.xiaoyv.blueprint.base.binding.BaseBindingActivity
import com.xiaoyv.blueprint.base.rxjava.subscribes
import com.xiaoyv.business.config.NavigationKey
import com.xiaoyv.business.config.NavigationPath
import com.xiaoyv.business.room.database.DateBaseManger
import com.xiaoyv.business.room.entity.SshEntity
import com.xiaoyv.business.rx.RxEventStack
import com.xiaoyv.business.rx.RxEventTag
import com.xiaoyv.desktop.ssh.R
import com.xiaoyv.desktop.ssh.databinding.SshAddActivitiyBinding
import com.xiaoyv.widget.utils.doOnBarClick
import io.reactivex.rxjava3.core.Observable

/**
 * AddSshActivity
 *
 * @author why
 * @since 2020/12/07
 */
@Route(path = NavigationPath.PATH_SSH_ADD_ACTIVITY)
class AddSshActivity : BaseBindingActivity<SshAddActivitiyBinding>() {
    private lateinit var sshEntity: SshEntity

    private var isAdd: Boolean = false

    override fun createContentBinding(layoutInflater: LayoutInflater): SshAddActivitiyBinding {
        return SshAddActivitiyBinding.inflate(layoutInflater)
    }

    override fun initIntentData(intent: Intent, bundle: Bundle, isNewIntent: Boolean) {
        sshEntity = getIntent().getSerializableExtra(NavigationKey.KEY_SERIALIZABLE) as? SshEntity
            ?: run {
                isAdd = true

                SshEntity().also {
                    it.label = "远程SSH"
                    it.group = StringUtils.getString(R.string.ssh_add_group_default)
                    it.port = StringUtils.getString(R.string.ssh_add_port_default)
                }
            }
    }

    override fun initView() {
        binding.toolbar.title =
            (if (isAdd) getString(R.string.ssh_add_title) else getString(R.string.ssh_add_edit_title))

        binding.toolbar.setRightIcon(
            R.drawable.ui_icon_save,
            onBarClickListener = doOnBarClick { _, _ ->
                doSaveConfig()
            })
    }

    override fun initData() {
        binding.asvLabel.uiValue = sshEntity.label
        binding.asvGroup.uiValue = sshEntity.group
        binding.asvIp.uiValue = sshEntity.ip
        binding.asvPort.uiValue = sshEntity.port
        binding.asvAccount.uiValue = sshEntity.account
        binding.asvPassword.uiValue = sshEntity.password
    }

    override fun initListener() {

    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (isAdd) {
            super.onBackPressed()
        } else {
            doSaveConfig()
        }
    }

    private fun doSaveConfig() {
        val label = binding.asvLabel.uiValue.orEmpty()
        val group = binding.asvGroup.uiValue.orEmpty()
        val ip = binding.asvIp.uiValue.orEmpty()
        val port = binding.asvPort.uiValue.orEmpty()
        val account = binding.asvAccount.uiValue.orEmpty()
        val password = binding.asvPassword.uiValue.orEmpty()

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

        val entity = sshEntity.also {
            it.label = label
            it.group = group
            it.ip = ip
            it.port = port
            it.account = account
            it.password = password
            it.domain = ip
        }

        saveSshBookmark(entity)
            .compose(bindTransformer())
            .to(bindLifecycle())
            .subscribes(
                onSuccess = {
                    RxEventStack.postEmpty(RxEventTag.EVENT_ADD_SSH_SUCCESS)

                    finish()
                },
                onError = {
                    ToastUtils.showShort("配置添加或更新失败")
                    finish()
                }
            )
    }

    /**
     * 保存书签
     *
     * @param sshEntity 配置的连接信息
     */
    private fun saveSshBookmark(sshEntity: SshEntity): Observable<Boolean> {
        return Observable.create {
            DateBaseManger.get().saveSsh(sshEntity)
            it.onNext(true)
            it.onComplete()
        }
    }

    companion object {
        /**
         * 编辑
         *
         * @param sshEntity 实体
         */
        fun openSelf(sshEntity: SshEntity?) {
            val intent = Intent(Utils.getApp(), AddSshActivity::class.java)
            intent.putExtra(NavigationKey.KEY_SERIALIZABLE, sshEntity)
            ActivityUtils.startActivity(intent)
        }
    }
}