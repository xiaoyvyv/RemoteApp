package com.xiaoyv.rdp.add

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.*
import com.blankj.utilcode.util.ThreadUtils.SimpleTask
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.busines.base.BaseActivity
import com.xiaoyv.busines.config.NavigationPath
import com.xiaoyv.busines.room.database.DateBaseManger
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.rdp.R
import com.xiaoyv.rdp.databinding.RdpActivityAddBinding

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

    override fun createContentView(): View {
        binding = RdpActivityAddBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initIntentData(intent: Intent, bundle: Bundle) {
        rdpEntity = (getIntent().getSerializableExtra(KEY_RDP_ENTITY) as? RdpEntity) ?: RdpEntity()
            .also {
                it.label = "远程桌面"
                it.group = StringUtils.getString(R.string.rdp_add_group_default)
                it.port = StringUtils.getString(R.string.rdp_add_port_default)
            }

        // 获取配置信息
        rdpEntity.configStr?.let {
            if (it.isNotEmpty()) {
                rdpConfig = GsonUtils.fromJson(it, RdpConfig::class.java)
            }
        }
    }

    override fun initView() {
        LogUtils.json(rdpEntity)

        binding.asvLabel.setTitle(StringUtils.getString(R.string.rdp_add_label))
            .setHint(StringUtils.getString(R.string.rdp_add_label_hint))
            .setMessage(rdpEntity.label)
            .setMessageHint(getString(R.string.rdp_add_label_required))

        binding.asvGroup.setTitle(StringUtils.getString(R.string.rdp_add_group))
            .setHint(StringUtils.getString(R.string.rdp_add_group_hint))
            .setMessage(rdpEntity.group)
            .setMessageHint()

        binding.asvIp.setTitle(StringUtils.getString(R.string.rdp_add_ip))
            .setHint(getString(R.string.rdp_add_ip_hint))
            .setMessage(rdpEntity.ip)
            .setMessageHint(getString(R.string.rdp_add_ip_required))

        binding.asvPort.setTitle(StringUtils.getString(R.string.rdp_add_port))
            .setInputNumberType(5)
            .setHint(StringUtils.getString(R.string.rdp_add_port_hint))
            .setMessage(rdpEntity.port)
            .setMessageHint()

        binding.asvAccount.setTitle(StringUtils.getString(R.string.rdp_add_account))
            .setHint(StringUtils.getString(R.string.rdp_add_account_hint))
            .setMessage(rdpEntity.account)
            .setMessageHint(getString(R.string.rdp_add_account_required))

        binding.asvPassword.setTitle(StringUtils.getString(R.string.rdp_add_password))
            .setHint(StringUtils.getString(R.string.rdp_add_password_hint))
            .setMessage(rdpEntity.password)
            .setMessageHint("未设置密码请留空", ColorUtils.getColor(com.xiaoyv.ui.R.color.ui_text_c3))
    }

    override fun initData() {
        binding.toolbar.setTitle(getString(R.string.rdp_add_title))
            .setStartClickListener { onBackPressed() }
            .setEndIcon(R.drawable.ui_icon_save)
            .setEndClickListener {
                val label = binding.asvLabel.message
                val group = binding.asvGroup.message
                val ip = binding.asvIp.message
                val port = binding.asvPort.message
                val account = binding.asvAccount.message
                val password = binding.asvPassword.message

                if (StringUtils.isEmpty(label)) {
                    p2vShowToast(StringUtils.getString(R.string.rdp_add_label_empty))
                    return@setEndClickListener
                }
                if (StringUtils.isEmpty(group)) {
                    p2vShowToast(StringUtils.getString(R.string.rdp_add_group_empty))
                    return@setEndClickListener
                }
                if (StringUtils.isEmpty(ip)) {
                    p2vShowToast(StringUtils.getString(R.string.rdp_add_ip_empty))
                    return@setEndClickListener
                }
                if (StringUtils.isEmpty(port)) {
                    p2vShowToast(StringUtils.getString(R.string.rdp_add_port_empty))
                    return@setEndClickListener
                }
                if (StringUtils.isEmpty(account)) {
                    p2vShowToast(StringUtils.getString(R.string.rdp_add_account_empty))
                    return@setEndClickListener
                }

                val rdpEntity = RdpEntity()
                rdpEntity.label = label
                rdpEntity.group = group
                rdpEntity.ip = ip
                rdpEntity.port = port
                rdpEntity.account = account
                rdpEntity.password = password
                rdpEntity.domain = ip
                saveRdpBookmark(rdpEntity)
            }
    }

    /**
     * 保存书签
     *
     * @param rdpEntity 配置的连接信息
     */
    private fun saveRdpBookmark(rdpEntity: RdpEntity) {
        // 保存配置信息
        ThreadUtils.executeByCached(object : SimpleTask<Boolean>() {
            @Throws(NumberFormatException::class)
            override fun doInBackground(): Boolean {
                rdpConfig.hostname = rdpEntity.ip
                rdpConfig.port = rdpEntity.port.toInt()
                rdpConfig.label = rdpEntity.label
                rdpConfig.username = rdpEntity.account
                rdpConfig.password = rdpEntity.password
                rdpConfig.domain = rdpEntity.domain
                rdpEntity.configStr = GsonUtils.toJson(rdpConfig)
                DateBaseManger.get().saveRdp(rdpEntity)
                return true
            }

            override fun onSuccess(result: Boolean) {
                onBackPressed()
            }
        })
    }

    companion object {
        const val KEY_RDP_ENTITY = "BOOK_MARK"

        /**
         * 编辑
         *
         * @param rdpEntity 实体
         */
        @JvmStatic
        fun openSelf(rdpEntity: RdpEntity?) {
            val intent = Intent(Utils.getApp(), AddRdpActivity::class.java)
            intent.putExtra(KEY_RDP_ENTITY, rdpEntity)
            ActivityUtils.startActivity(intent)
        }
    }
}