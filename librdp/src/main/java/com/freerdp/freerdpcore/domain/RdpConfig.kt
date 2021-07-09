package com.freerdp.freerdpcore.domain

import com.blankj.utilcode.util.ScreenUtils
import java.io.Serializable

/**
 * RdpConfig
 *
 * @author why
 * @since 2021/07/03
 **/
class RdpConfig : Serializable {
    var screenSettings: ActiveScreenSettings = ActiveScreenSettings()
    var advancedSettings: AdvancedSettings = AdvancedSettings()
    var debugSettings: DebugSettings = DebugSettings()
    var performanceSettings: PerformanceSettings = PerformanceSettings()
    var gatewaySettings: GatewaySettings = GatewaySettings()

    var enableGatewaySettings: Boolean = false
    var clientName: String = "客户端"
    var hostname: String = "192.168.31.202"
    var port: Int = 3389
    var certName: String = ""
    var username: String = "Administrator"
    var domain: String = ""
    var password: String = ""
    var label: String = ""

    /**
     * 视频设置
     */
    class ActiveScreenSettings {
        private var resolution = AUTOMATIC
            get() {
                when (field) {
                    FITSCREEN, AUTOMATIC, CUSTOM, PREDEFINED -> {
                    }
                    else -> field = AUTOMATIC
                }
                return field
            }

        var colors: Int = 32
            get() {
                when (field) {
                    32, 24, 16, 15, 8 -> {
                    }
                    else -> field = 32
                }
                return field
            }

        var width = ScreenUtils.getScreenWidth()
            get() {
                if (field <= 0 || field > 65536) {
                    field = 1024
                }
                return field
            }

        var height = ScreenUtils.getScreenHeight()
            get() {
                if (field <= 0 || field > 65536) {
                    field = 768
                }
                return field
            }

        fun setResolution(resolution: Int) {
            this.resolution = resolution
            if (resolution == AUTOMATIC || resolution == FITSCREEN) {
                width = 0
                height = 0
            }
        }

        fun setResolution(resolution: String, width: Int, height: Int) {
            when {
                resolution.contains("x") -> {
                    val dimensions = resolution.split("x").toTypedArray()
                    this.width = dimensions[0].toInt()
                    this.height = dimensions[1].toInt()
                    this.resolution = PREDEFINED
                }
                resolution.equals("custom", ignoreCase = true) -> {
                    this.width = width
                    this.height = height
                    this.resolution = CUSTOM
                }
                // 连接时定义
                resolution.equals("fitscreen", ignoreCase = true) -> {
                    this.height = 0
                    this.width = 0
                    this.resolution = FITSCREEN
                }
                // 连接时定义
                else -> {
                    this.height = 0
                    this.width = 0
                    this.resolution = AUTOMATIC
                }
            }
        }

        fun getResolutionString(): String {
            return when {
                isPredefined() -> {
                    width.toString() + "x" + height
                }
                isFitScreen() -> {
                    "fitscreen"
                }
                isAutomatic() -> {
                    "automatic"
                }
                else -> {
                    "custom"
                }
            }
        }

        fun isPredefined(): Boolean {
            return resolution == PREDEFINED
        }

        fun isAutomatic(): Boolean {
            return resolution == AUTOMATIC
        }

        fun isFitScreen(): Boolean {
            return resolution == FITSCREEN
        }

        fun isCustom(): Boolean {
            return resolution == CUSTOM
        }

        companion object {
            const val FITSCREEN = -2
            const val AUTOMATIC = -1
            const val CUSTOM = 0
            const val PREDEFINED = 1
        }
    }

    /**
     * 高级设置
     */
    class AdvancedSettings {
        var remoteProgram: String = ""
        var workDir: String = ""
        var consoleMode: Boolean = false
        var redirectSDCard: Boolean = false
        var redirectMicrophone: Boolean = false

        var redirectSound: Int = 0
            get() {
                when (field) {
                    0, 1, 2 -> {
                    }
                    else -> field = 0
                }
                return field
            }

        var security: Int = 3
            get() {
                when (field) {
                    0, 1, 2, 3 -> {
                    }
                    else -> field = 0
                }
                return field
            }
    }

    /**
     * 调试设置
     */
    class DebugSettings {
        var debugLevel = "TRACE"
            get() {
                for (level in levels) {
                    if (level.equals(field, ignoreCase = true)) {
                        return field
                    }
                }
                field = "INFO"
                return field
            }

        var asyncChannel: Boolean = true
        var asyncTransport: Boolean = false
        var asyncInput: Boolean = true
        var asyncUpdate: Boolean = true

        companion object {
            @JvmStatic
            val levels = arrayOf("OFF", "FATAL", "ERROR", "WARN", "INFO", "DEBUG", "TRACE")
        }
    }

    /**
     * 性能设置
     */
    class PerformanceSettings {
        var remoteFx = true
        var gfx = true
        var h264 = true
        var wallpaper = false
        var theme = true
        var fullWindowDrag = false
        var menuAnimations = false
        var fontSmoothing = false
        var desktopComposition = false
    }

    /**
     * 网关设置
     */
    class GatewaySettings {
        var hostname: String = ""
        var port: Int = 443
        var username: String = ""
        var domain: String = ""
        var password: String = ""
    }
}