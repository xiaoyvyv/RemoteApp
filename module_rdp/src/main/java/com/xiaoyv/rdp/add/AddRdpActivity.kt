package com.xiaoyv.rdp.add

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.*
import com.blankj.utilcode.util.ThreadUtils.SimpleTask
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.busines.base.BaseActivity
import com.xiaoyv.busines.config.NavigationKey
import com.xiaoyv.busines.config.NavigationPath
import com.xiaoyv.busines.room.database.DateBaseManger
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.rdp.R
import com.xiaoyv.rdp.databinding.RdpActivityAddBinding
import com.xiaoyv.rdp.setting.single.RdpSingleSettingActivity

/**
 * AddRdpActivity
 *
 * @author why
 * @since 2020/11/29
 */
@Route(path = NavigationPath.PATH_RDP_ADD_ACTIVITY)
class AddRdpActivity : BaseActivity() {
    private lateinit var binding: RdpActivityAddBinding
    private lateinit var rdpEntity: RdpEntity
    private var rdpConfig: RdpConfig = RdpConfig()
    private var isAdd = false

    override fun createContentView(): View {
        binding = RdpActivityAddBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initIntentData(intent: Intent, bundle: Bundle) {
        rdpEntity = (intent.getSerializableExtra(NavigationKey.KEY_SERIALIZABLE) as? RdpEntity)
            ?: run {
                isAdd = true
                RdpEntity().also {
                    it.label = StringUtils.getString(R.string.rdp_add_label_default)
                    it.group = StringUtils.getString(R.string.rdp_add_group_default)
                    it.port = StringUtils.getString(R.string.rdp_add_port_default)
                }
            }

        // 获取配置信息
        rdpEntity.configStr?.let {
            if (it.isNotEmpty()) {
                rdpConfig = GsonUtils.fromJson(it, RdpConfig::class.java)
            }
        }
    }

    override fun initView() {
        LogUtils.e(isAdd, GsonUtils.toJson(rdpEntity))

        binding.asvLabel.uiValue = rdpEntity.label.orEmpty()
        binding.asvGroup.uiValue = rdpEntity.group.orEmpty()
        binding.asvIp.uiValue = rdpEntity.ip.orEmpty()
        binding.asvDomain.uiValue = rdpEntity.domain.orEmpty()
        binding.asvPort.uiValue = rdpEntity.port.orEmpty()
        binding.asvAccount.uiValue = rdpEntity.account.orEmpty()
        binding.asvPassword.uiValue = rdpEntity.password.orEmpty()
    }

    override fun initData() {
        binding.toolbar.setTitle(getString(R.string.rdp_add_title))
            .setStartClickListener { onBackPressed() }
            .setEndIcon(R.drawable.ui_icon_save)
            .setEndClickListener {
                doSaveConfig()
            }
    }

    override fun initListener() {
        // 分辨率设置
        binding.setScreen.setOnClickListener {
            RdpSingleSettingActivity.openSelf(
                this,
                rdpConfig, RdpSingleSettingActivity.SETTING_SCREEN
            )
        }

        // 性能设置
        binding.setPerformance.setOnClickListener {
            RdpSingleSettingActivity.openSelf(
                this,
                rdpConfig, RdpSingleSettingActivity.SETTING_PERFORMANCE
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == 999 && data != null) {
            val newConfig = data.getSerializableExtra(NavigationKey.KEY_SERIALIZABLE) as? RdpConfig
            newConfig?.let {
                ToastUtils.showShort("更新配置")
                LogUtils.e(GsonUtils.toJson(it))
                rdpConfig = it
            }
        }
    }

    override fun onBackPressed() {
        if (!isAdd) {
            doSaveConfig()
        } else {
            super.onBackPressed()
        }
    }


    private fun doSaveConfig(): Boolean {
        val label = binding.asvLabel.uiValue
        val group = binding.asvGroup.uiValue
        val ip = binding.asvIp.uiValue
        val domain = binding.asvDomain.uiValue
        val port = binding.asvPort.uiValue
        val account = binding.asvAccount.uiValue
        val password = binding.asvPassword.uiValue

        if (StringUtils.isEmpty(label)) {
            p2vShowToast(StringUtils.getString(R.string.rdp_add_label_empty))
            return false
        }
        if (StringUtils.isEmpty(group)) {
            p2vShowToast(StringUtils.getString(R.string.rdp_add_group_empty))
            return false
        }
        if (StringUtils.isEmpty(ip)) {
            p2vShowToast(StringUtils.getString(R.string.rdp_add_ip_empty))
            return false
        }
        if (StringUtils.isEmpty(port)) {
            p2vShowToast(StringUtils.getString(R.string.rdp_add_port_empty))
            return false
        }
        if (StringUtils.isEmpty(account)) {
            p2vShowToast(StringUtils.getString(R.string.rdp_add_account_empty))
            return false
        }

        saveRdpBookmark(rdpEntity.also {
            it.label = label
            it.group = group
            it.ip = ip
            it.domain = domain
            it.port = port
            it.account = account
            it.password = password

            it.configStr = rdpConfig.also { config ->
                config.label = it.label
                config.hostname = it.ip
                config.domain = it.domain
                config.port = it.port.toInt()
                config.username = it.account
                config.password = it.password
            }.toJson()
        })
        return true
    }

    /**
     * 保存配置信息
     *
     * @param rdpEntity 配置的连接信息
     */
    private fun saveRdpBookmark(rdpEntity: RdpEntity) {
        ThreadUtils.executeByCached(object : SimpleTask<Boolean>() {
            @Throws(NumberFormatException::class)
            override fun doInBackground(): Boolean {
                DateBaseManger.get().saveRdp(rdpEntity)
                return true
            }

            override fun onFail(t: Throwable?) {
                ToastUtils.showShort("配置添加或更新失败")
            }

            override fun onSuccess(result: Boolean) {
                finish()
            }
        })
    }

    companion object {
        /**
         * 编辑
         *
         * @param rdpEntity 实体
         */
        @JvmStatic
        fun openSelf(rdpEntity: RdpEntity?) {
            val intent = Intent(Utils.getApp(), AddRdpActivity::class.java)
            intent.putExtra(NavigationKey.KEY_SERIALIZABLE, rdpEntity)
            ActivityUtils.startActivity(intent)
        }
    }
}