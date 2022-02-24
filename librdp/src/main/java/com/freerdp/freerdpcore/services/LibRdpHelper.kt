package com.freerdp.freerdpcore.services

import android.net.Uri
import com.blankj.utilcode.util.PathUtils
import com.freerdp.freerdpcore.domain.RdpConfig
import java.util.*

/**
 * LibRdpHelper
 *
 * @author why
 * @since 2021/07/03
 **/
object LibRdpHelper {

    /**
     * 配置连接信息
     *
     * @param rdpConfig rdpConfig
     * @return 是否成功
     */
    @JvmStatic
    fun setConnectionInfo(rdpConfig: RdpConfig): Array<String> {
        val arguments = ArrayList<String>()

        // 现在我们只支持软件 GDI
        arguments.add(LibFreeRDP.TAG)
        arguments.add("/gdi:sw")

        // 获取参数配置
        val clientName = rdpConfig.clientName
        val hostname = rdpConfig.hostname
        val port = rdpConfig.port
        val certName = rdpConfig.certName
        val username = rdpConfig.username
        val domain = rdpConfig.domain
        val password = rdpConfig.password

        // 其他配置
        val screenSettings = rdpConfig.screenSettings
        val advancedSettings = rdpConfig.advancedSettings
        val debugSettings = rdpConfig.debugSettings
        val performanceSettings = rdpConfig.performanceSettings

        // 客户端名称
        if (clientName.isNotEmpty()) {
            arguments.add("/client-hostname:$clientName")
        }

        // 域名和端口
        arguments.add("/v:$hostname")
        arguments.add("/port:$port")

        // 用戶名
        if (username.isNotEmpty()) {
            arguments.add("/u:$username")
        }
        // 域名
        if (domain.isNotEmpty()) {
            arguments.add("/d:$domain")
        }
        // 密码
        if (password.isNotEmpty()) {
            arguments.add("/p:$password")
        }
        // 证书
        if (certName.isNotEmpty()) {
            arguments.add("/cert-name:$certName")
        }

        // 分辨率
        arguments.add(
            String.format("/size:%dx%d", screenSettings.width, screenSettings.height)
        )
        // 色彩
        arguments.add("/bpp:" + screenSettings.colors)

        if (advancedSettings.consoleMode) {
            arguments.add("/admin")
        }
        when (advancedSettings.security) {
            // NLA
            3 -> arguments.add("/sec-nla")
            // TLS
            2 -> arguments.add("/sec-tls")
            // RDP
            1 -> arguments.add("/sec-rdp")
            else -> {
            }
        }

        // 性能配置
        if (performanceSettings.remoteFx) {
            arguments.add("/rfx")
        }
        if (performanceSettings.gfx) {
            arguments.add("/gfx")
        }
        if (performanceSettings.h264 && LibFreeRDP.mHasH264) {
            arguments.add("/gfx:AVC444")
        }
        arguments.add(LibFreeRDP.addFlag("wallpaper", performanceSettings.wallpaper))
        arguments.add(LibFreeRDP.addFlag("window-drag", performanceSettings.fullWindowDrag))
        arguments.add(LibFreeRDP.addFlag("menu-anims", performanceSettings.menuAnimations))
        arguments.add(LibFreeRDP.addFlag("themes", performanceSettings.theme))
        arguments.add(LibFreeRDP.addFlag("fonts", performanceSettings.fontSmoothing))
        arguments.add(LibFreeRDP.addFlag("aero", performanceSettings.desktopComposition))
        arguments.add(LibFreeRDP.addFlag("glyph-cache", false))

        if (advancedSettings.remoteProgram.isNotEmpty()) {
            arguments.add("/shell:" + advancedSettings.remoteProgram)
        }
        if (advancedSettings.workDir.isNotEmpty()) {
            arguments.add("/shell-dir:" + advancedSettings.workDir)
        }
        arguments.add(LibFreeRDP.addFlag("async-channels", debugSettings.asyncChannel))
        arguments.add(LibFreeRDP.addFlag("async-input", debugSettings.asyncInput))
        arguments.add(LibFreeRDP.addFlag("async-update", debugSettings.asyncUpdate))

        // 内部储存重定向
        if (advancedSettings.redirectSDCard) {
            arguments.add("/drive:sdcard,${PathUtils.getExternalStoragePath()}")
        }
        // 剪切板重定向
        arguments.add("/clipboard")

        // 网关启用
        if (rdpConfig.enableGatewaySettings) {
            val gateway = rdpConfig.gatewaySettings
            // 网关地址、端口
            arguments.add(
                String.format(
                    Locale.getDefault(), "/g:%s:%d", gateway.hostname, gateway.port
                )
            )
            // 用户名
            val gUsername = gateway.username
            if (gUsername.isNotEmpty()) {
                arguments.add("/gu:$gUsername")
            }
            // 域名
            val gDomain = gateway.domain
            if (gDomain.isNotEmpty()) {
                arguments.add("/gd:$gDomain")
            }
            // 密码
            val gPassword = gateway.password
            if (gPassword.isNotEmpty()) {
                arguments.add("/gp:$gPassword")
            }
        }

        /* 0 ... local
		   1 ... remote
		   2 ... disable */
        arguments.add("/audio-mode:" + advancedSettings.redirectSound.toString())
        if (advancedSettings.redirectSound == 0) {
            arguments.add("/sound")
        }
        if (advancedSettings.redirectMicrophone) {
            arguments.add("/microphone")
        }

        // arguments.add("/cert-ignore")

        arguments.add("/log-level:" + debugSettings.debugLevel)
        return arguments.toTypedArray()
    }

    /**
     * 从查询字符串解析 URI。同一个键覆盖前一个
     *
     * freerdp://user@ip:port/connect?sound=&rfx=&p=password&clipboard=%2b&themes=-
     */
    @JvmStatic
    fun setConnectionInfo(openUri: Uri): Array<String> {
        val arguments = ArrayList<String>()

        // 现在我们只支持软件 GDI
        arguments.add(LibFreeRDP.TAG)
        arguments.add("/gdi:sw")

        // 客户端名称
        val clientName = "ClientName"
        if (clientName.isNotEmpty()) {
            arguments.add("/client-hostname:$clientName")
        }


        // 解析主机名和端口，设置为“v”参数
        var hostname = openUri.host
        val port = openUri.port
        if (hostname != null) {
            hostname += if (port == -1) "" else ":$port"
            arguments.add("/v:$hostname")
        }

        // 用户名
        val user = openUri.userInfo
        if (user != null) {
            arguments.add("/u:$user")
        }

        // 参数
        openUri.queryParameterNames.forEach { key ->
            openUri.getQueryParameter(key)?.let {
                if (it.isEmpty()) {
                    // 查询: key=
                    // 转为Rdp参数: /key
                    arguments.add("/$key")
                } else if (it == "-" || it == "+") {
                    // 查询: key=- or key=+
                    // 转为Rdp参数: -key or +key
                    arguments.add(it + key)
                } else {
                    // 查询: key=value
                    // 转为Rdp参数: /key:value
                    if (key == "drive" && it == "sdcard") {
                        // SD卡重定向专用
                        val path = PathUtils.getExternalStoragePath()
                        arguments.add("/$key:sdcard,$path")
                    } else {
                        arguments.add("/$key:$it")
                    }
                }
            }
        }
        return arguments.toTypedArray()
    }
}