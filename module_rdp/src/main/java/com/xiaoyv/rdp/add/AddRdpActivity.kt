package com.xiaoyv.rdp.add

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.alibaba.android.arouter.facade.annotation.Route
import com.blankj.utilcode.util.*
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.blueprint.base.BaseActivity
import com.xiaoyv.blueprint.base.rxjava.subscribes
import com.xiaoyv.busines.config.NavigationKey
import com.xiaoyv.busines.config.NavigationPath
import com.xiaoyv.busines.room.database.DateBaseManger
import com.xiaoyv.busines.room.entity.RdpEntity
import com.xiaoyv.busines.rx.RxEventStack
import com.xiaoyv.busines.rx.RxEventTag
import com.xiaoyv.desktop.rdp.R
import com.xiaoyv.desktop.rdp.databinding.RdpActivityAddBinding
import com.xiaoyv.rdp.setting.single.RdpSingleSettingActivity
import com.xiaoyv.widget.utils.doOnBarClick
import io.reactivex.rxjava3.core.Observable

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
    private var edit: Boolean = false
    private var isAdd: Boolean = false

    private val screenLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            edit = true
            val screenSettings =
                it.data?.getSerializableExtra(NavigationKey.KEY_SERIALIZABLE) as? RdpConfig.ActiveScreenSettings
                    ?: return@registerForActivityResult
            rdpConfig.screenSettings = screenSettings
        }

    private val advancedLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            edit = true
            val advancedSettings =
                it.data?.getSerializableExtra(NavigationKey.KEY_SERIALIZABLE) as? RdpConfig.AdvancedSettings
                    ?: return@registerForActivityResult
            rdpConfig.advancedSettings = advancedSettings
        }

    private val performanceLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            edit = true
            val performanceSettings =
                it.data?.getSerializableExtra(NavigationKey.KEY_SERIALIZABLE) as? RdpConfig.PerformanceSettings
                    ?: return@registerForActivityResult
            rdpConfig.performanceSettings = performanceSettings
        }

    private val debugLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            edit = true
            val debugSettings =
                it.data?.getSerializableExtra(NavigationKey.KEY_SERIALIZABLE) as? RdpConfig.DebugSettings
                    ?: return@registerForActivityResult
            rdpConfig.debugSettings = debugSettings
        }

    override fun createContentView(): View {
        binding = RdpActivityAddBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initIntentData(intent: Intent, bundle: Bundle, isNewIntent: Boolean) {
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
        rdpEntity.configStr.let {
            if (it.isNotEmpty()) {
                rdpConfig = GsonUtils.fromJson(it, RdpConfig::class.java)
            }
        }
    }


    override fun initView() {
        binding.toolbar.title =
            (if (isAdd) getString(R.string.rdp_add_title) else getString(R.string.rdp_add_edit_title))
        binding.toolbar.setRightIcon(
            R.drawable.ui_icon_save,
            onBarClickListener = doOnBarClick { _, _ ->
                doSaveConfig()
            })
    }

    override fun initData() {
        binding.asvLabel.uiValue = rdpEntity.label
        binding.asvGroup.uiValue = rdpEntity.group
        binding.asvIp.uiValue = rdpEntity.ip
        binding.asvDomain.uiValue = rdpEntity.domain
        binding.asvPort.uiValue = rdpEntity.port
        binding.asvAccount.uiValue = rdpEntity.account
        binding.asvPassword.uiValue = rdpEntity.password
    }

    override fun initListener() {
        // 分辨率设置
        binding.setScreen.setOnClickListener {
            RdpSingleSettingActivity.openSelf(
                this, screenLauncher,
                rdpConfig, RdpSingleSettingActivity.SETTING_SCREEN
            )
        }

        // 性能设置
        binding.setPerformance.setOnClickListener {
            RdpSingleSettingActivity.openSelf(
                this, performanceLauncher,
                rdpConfig, RdpSingleSettingActivity.SETTING_PERFORMANCE
            )
        }

        // 高级设置
        binding.setAdvance.setOnClickListener {
            RdpSingleSettingActivity.openSelf(
                this, advancedLauncher,
                rdpConfig, RdpSingleSettingActivity.SETTING_ADVANCED
            )
        }
        // 高级设置
        binding.setDebug.setOnClickListener {
            RdpSingleSettingActivity.openSelf(
                this, debugLauncher,
                rdpConfig, RdpSingleSettingActivity.SETTING_DEBUG
            )
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        if (isAdd) {
            super.onBackPressed()
        } else {
            doSaveConfig()
        }
    }

    /**
     * 保存
     */
    private fun doSaveConfig() {
        val label = binding.asvLabel.uiValue.orEmpty()
        val group = binding.asvGroup.uiValue.orEmpty()
        val ip = binding.asvIp.uiValue.orEmpty()
        val domain = binding.asvDomain.uiValue.orEmpty()
        val port = binding.asvPort.uiValue.orEmpty()
        val account = binding.asvAccount.uiValue.orEmpty()
        val password = binding.asvPassword.uiValue.orEmpty()

        if (StringUtils.isEmpty(label)) {
            p2vShowToast(StringUtils.getString(R.string.rdp_add_label_empty))
            return
        }
        if (StringUtils.isEmpty(group)) {
            p2vShowToast(StringUtils.getString(R.string.rdp_add_group_empty))
            return
        }
        if (StringUtils.isEmpty(ip)) {
            p2vShowToast(StringUtils.getString(R.string.rdp_add_ip_empty))
            return
        }
        if (StringUtils.isEmpty(port)) {
            p2vShowToast(StringUtils.getString(R.string.rdp_add_port_empty))
            return
        }
        if (StringUtils.isEmpty(account)) {
            p2vShowToast(StringUtils.getString(R.string.rdp_add_account_empty))
            return
        }

        val rdpEntity = rdpEntity.also {
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
        }

        saveRdpBookmark(rdpEntity)
            .compose(bindTransformer())
            .to(bindLifecycle())
            .subscribes(
                onSuccess = {
                    RxEventStack.postEmpty(RxEventTag.EVENT_ADD_RDP_SUCCESS)

                    finish()
                },
                onError = {
                    ToastUtils.showShort("配置添加或更新失败")
                    finish()
                }
            )
    }

    /**
     * 保存配置信息
     *
     * @param rdpEntity 配置的连接信息
     */
    private fun saveRdpBookmark(rdpEntity: RdpEntity): Observable<Boolean> {
        return Observable.create {
            DateBaseManger.get().saveRdp(rdpEntity)
            it.onNext(true)
            it.onComplete()
        }
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