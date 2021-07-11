package com.xiaoyv.rdp.setting

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.StringUtils
import com.blankj.utilcode.util.Utils
import com.xiaoyv.busines.base.BaseActivity
import com.xiaoyv.busines.config.NavigationKey
import com.xiaoyv.rdp.R
import com.xiaoyv.rdp.databinding.RdpSettingClientBinding
import com.xiaoyv.rdp.databinding.RdpSettingPowerBinding
import com.xiaoyv.rdp.databinding.RdpSettingSecurityBinding
import com.xiaoyv.rdp.databinding.RdpSettingUiBinding

/**
 * UiSettingActivity
 *
 * @author why
 * @since 2020/12/04
 */
class RdpSettingActivity : BaseActivity() {
    private var type: String? = null
    private lateinit var uiBinding: RdpSettingUiBinding
    private lateinit var securityBinding: RdpSettingSecurityBinding
    private lateinit var powerBinding: RdpSettingPowerBinding
    private lateinit var clientBinding: RdpSettingClientBinding

    override fun createContentView(): View {
        return when {
            StringUtils.equals(TYPE_SETTING_UI, type) ->
                RdpSettingUiBinding.inflate(layoutInflater).let {
                    uiBinding = it
                    it.root
                }
            StringUtils.equals(TYPE_SETTING_POWER, type) ->
                RdpSettingPowerBinding.inflate(layoutInflater).let {
                    powerBinding = it
                    it.root
                }
            StringUtils.equals(TYPE_SETTING_SECURITY, type) ->
                RdpSettingSecurityBinding.inflate(layoutInflater).let {
                    securityBinding = it
                    it.root
                }
            StringUtils.equals(TYPE_SETTING_CLIENT, type) ->
                RdpSettingClientBinding.inflate(layoutInflater).let {
                    clientBinding = it
                    it.root
                }
            else -> throw IllegalStateException("Unexpected value: $type")
        }
    }

    override fun initIntentData(intent: Intent, bundle: Bundle) {
        type = getIntent().getStringExtra(NavigationKey.KEY_STRING)
    }

    override fun initView() {
        var title = StringUtils.getString(R.string.settings_cat_ui)

        when {
            StringUtils.equals(TYPE_SETTING_UI, type) -> {
                title = StringUtils.getString(R.string.settings_cat_ui)
            }
            StringUtils.equals(TYPE_SETTING_POWER, type) -> {
                title = StringUtils.getString(R.string.settings_cat_power)
            }
            StringUtils.equals(TYPE_SETTING_SECURITY, type) -> {
                title = StringUtils.getString(R.string.settings_cat_security)
            }
            StringUtils.equals(TYPE_SETTING_CLIENT, type) -> {
                title = StringUtils.getString(R.string.settings_cat_client)
            }
        }

        uiBinding.toolbar.setTitle(title)
            .setStartClickListener {
                onBackPressed()
            }
    }

    override fun initData() {}

    companion object {
        @JvmField
        var TYPE_SETTING_UI = "TYPE_SETTING_UI"

        @JvmField
        var TYPE_SETTING_SECURITY = "TYPE_SETTING_SECURITY"

        @JvmField
        var TYPE_SETTING_POWER = "TYPE_SETTING_POWER"

        @JvmField
        var TYPE_SETTING_CLIENT = "TYPE_SETTING_CLIENT"

        @JvmStatic
            fun openSelf(typeSetting: String) {
            val intent = Intent(Utils.getApp(), RdpSettingActivity::class.java)
            intent.putExtra(NavigationKey.KEY_STRING, typeSetting)
            ActivityUtils.startActivity(intent)
        }
    }
}