package com.xiaoyv.rdp.setting.single

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.IntDef
import com.blankj.utilcode.util.ActivityUtils
import com.blankj.utilcode.util.FragmentUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.freerdp.freerdpcore.domain.RdpConfig
import com.xiaoyv.busines.base.BaseActivity
import com.xiaoyv.busines.config.NavigationKey
import com.xiaoyv.rdp.databinding.RdpSettingSingleBinding

/**
 * RdpSingleSettingActivity
 *
 * @author why
 * @since 2021/07/11
 **/
class RdpSingleSettingActivity : BaseActivity() {
    private lateinit var binding: RdpSettingSingleBinding
    var rdpConfig: RdpConfig? = null

    @SingleSettingType
    private var type: Int = SETTING_SCREEN

    /**
     * 导入视图
     *
     * @return 视图
     */
    override fun createContentView(): View {
        binding = RdpSettingSingleBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun initIntentData(intent: Intent, bundle: Bundle) {
        rdpConfig = bundle.getSerializable(NavigationKey.KEY_SERIALIZABLE) as? RdpConfig
        if (rdpConfig == null) {
            ToastUtils.showShort("配置文件出错！")
            onBackPressed()
            return
        }
        this.type = bundle.getInt(NavigationKey.KEY_INT, type)
    }

    /**
     * 初始化
     */
    override fun initView() {
        when (type) {
            SETTING_SCREEN -> {
                FragmentUtils.add(
                    supportFragmentManager,
                    RdpSingleScreenFragment.newInstance(),
                    binding.flContainer.id, false
                )
            }
            SETTING_PERFORMANCE -> {
                FragmentUtils.add(
                    supportFragmentManager,
                    RdpSinglePerformanceFragment.newInstance(),
                    binding.flContainer.id, false
                )
            }
            SETTING_DEBUG -> {
                FragmentUtils.add(
                    supportFragmentManager,
                    RdpSingleDebugFragment.newInstance(),
                    binding.flContainer.id, false
                )
            }
            SETTING_ADVANCED -> {
                FragmentUtils.add(
                    supportFragmentManager,
                    RdpSingleAdvancedFragment.newInstance(),
                    binding.flContainer.id, false
                )
            }
        }
    }

    /**
     * 初始化数据
     */
    override fun initData() {

    }

    override fun onBackPressed() {
        intent?.let {
            rdpConfig?.apply {
                it.putExtra(NavigationKey.KEY_SERIALIZABLE, this)
                setResult(RESULT_OK, it)
            }
        }
        super.onBackPressed()
    }

    companion object {
        const val SETTING_SCREEN = 0
        const val SETTING_PERFORMANCE = 1
        const val SETTING_DEBUG = 2
        const val SETTING_ADVANCED = 3

        @JvmStatic
        fun openSelf(activity: Activity, rdpConfig: RdpConfig, @SingleSettingType type: Int) {
            val intent = Intent(Utils.getApp(), RdpSingleSettingActivity::class.java)
            intent.putExtra(NavigationKey.KEY_SERIALIZABLE, rdpConfig)
            intent.putExtra(NavigationKey.KEY_INT, type)
            ActivityUtils.startActivityForResult(activity, intent, 999)
        }
    }

    @IntDef(
        SETTING_SCREEN,
        SETTING_PERFORMANCE,
        SETTING_DEBUG,
        SETTING_ADVANCED
    )
    @Retention(AnnotationRetention.SOURCE)
    annotation class SingleSettingType
}