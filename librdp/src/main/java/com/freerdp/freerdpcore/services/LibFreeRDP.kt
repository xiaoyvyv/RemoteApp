@file:Suppress("unused", "FunctionName")

package com.freerdp.freerdpcore.services

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.collection.LongSparseArray
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.PathUtils
import com.freerdp.freerdpcore.application.RdpApp
import com.freerdp.freerdpcore.domain.RdpConfig

object LibFreeRDP {
    const val TAG = "LibFreeRDP"

    const val VERIFY_CERT_FLAG_NONE: Long = 0x00
    const val VERIFY_CERT_FLAG_LEGACY: Long = 0x02
    const val VERIFY_CERT_FLAG_REDIRECT: Long = 0x10
    const val VERIFY_CERT_FLAG_GATEWAY: Long = 0x20
    const val VERIFY_CERT_FLAG_CHANGED: Long = 0x40
    const val VERIFY_CERT_FLAG_MISMATCH: Long = 0x80
    const val VERIFY_CERT_FLAG_MATCH_LEGACY_SHA1: Long = 0x100
    const val VERIFY_CERT_FLAG_FP_IS_PEM: Long = 0x200

    @JvmStatic
    var mHasH264 = true

    @JvmStatic
    private var listenerLibRdp: LibRdpEventListener? = null

    @JvmStatic
    private val mInstanceState = LongSparseArray<Boolean>()

    @JvmStatic
    private val lock = Object()

    @JvmStatic
    fun hasH264Support(): Boolean {
        return mHasH264
    }

    private external fun freerdp_get_jni_version(): String?
    private external fun freerdp_get_version(): String?
    private external fun freerdp_get_build_date(): String?
    private external fun freerdp_get_build_revision(): String?
    private external fun freerdp_get_build_config(): String?
    private external fun freerdp_new(context: Context): Long
    private external fun freerdp_free(inst: Long)
    private external fun freerdp_parse_arguments(inst: Long, args: Array<String>): Boolean
    private external fun freerdp_connect(inst: Long): Boolean
    private external fun freerdp_disconnect(inst: Long): Boolean
    private external fun freerdp_update_graphics(
        inst: Long, bitmap: Bitmap, x: Int, y: Int, width: Int, height: Int
    ): Boolean

    private external fun freerdp_send_cursor_event(inst: Long, x: Int, y: Int, flags: Int): Boolean
    private external fun freerdp_send_key_event(inst: Long, keycode: Int, down: Boolean): Boolean
    private external fun freerdp_send_unicodekey_event(
        inst: Long, keycode: Int, down: Boolean
    ): Boolean

    private external fun freerdp_send_clipboard_data(inst: Long, data: String): Boolean
    private external fun freerdp_get_last_error_string(inst: Long): String?

    @JvmStatic
    fun setEventListener(libRdpEventListener: LibRdpEventListener?) {
        listenerLibRdp = libRdpEventListener
    }

    @JvmStatic
    fun newInstance(context: Context): Long = freerdp_new(context)

    @JvmStatic
    fun freeInstance(inst: Long) {
        synchronized(lock) {
            if (mInstanceState[inst, false]) {
                freerdp_disconnect(inst)
            }
            while (mInstanceState[inst, false]) {
                try {
                    lock.wait()
                } catch (e: InterruptedException) {
                    throw RuntimeException()
                }
            }
        }
        freerdp_free(inst)
    }

    @JvmStatic
    fun connect(inst: Long): Boolean {
        synchronized(lock) {
            if (mInstanceState[inst, false]) {
                throw RuntimeException("instance already connected")
            }
        }
        return freerdp_connect(inst)
    }

    @JvmStatic
    fun disconnect(inst: Long): Boolean {
        synchronized(lock) {
            return if (mInstanceState[inst, false]) {
                freerdp_disconnect(inst)
            } else true
        }
    }

    @JvmStatic
    fun cancelConnection(inst: Long): Boolean {
        synchronized(lock) {
            return if (mInstanceState[inst, false]) {
                freerdp_disconnect(inst)
            } else true
        }
    }

    @JvmStatic
    fun addFlag(name: String, enabled: Boolean): String {
        return if (enabled) "+$name" else "-$name"
    }

    /**
     * 配置连接信息
     *
     * @param inst      实例ID
     * @param rdpConfig bookmark
     * @return 是否成功
     */
    @JvmStatic
    fun setConnectionInfo(inst: Long, rdpConfig: RdpConfig): Boolean {
        val arguments = LibRdpHelper.setConnectionInfo(rdpConfig)
        LogUtils.e("Rdp 连接参数", arguments)
        return freerdp_parse_arguments(inst, arguments)
    }

    @JvmStatic
    fun setConnectionInfo(inst: Long, openUri: Uri): Boolean {
        val arguments = LibRdpHelper.setConnectionInfo(openUri)
        LogUtils.e("Rdp 连接参数", arguments)
        return freerdp_parse_arguments(inst, arguments)
    }

    @JvmStatic
    fun updateGraphics(
        inst: Long, bitmap: Bitmap, x: Int, y: Int, width: Int,
        height: Int
    ): Boolean {
        return freerdp_update_graphics(inst, bitmap, x, y, width, height)
    }

    @JvmStatic
    fun sendCursorEvent(inst: Long, x: Int, y: Int, flags: Int): Boolean {
        return freerdp_send_cursor_event(inst, x, y, flags)
    }

    @JvmStatic
    fun sendKeyEvent(inst: Long, keycode: Int, down: Boolean): Boolean {
        return freerdp_send_key_event(inst, keycode, down)
    }

    @JvmStatic
    fun sendUnicodeKeyEvent(inst: Long, keycode: Int, down: Boolean): Boolean {
        return freerdp_send_unicodekey_event(inst, keycode, down)
    }

    @JvmStatic
    fun sendClipboardData(inst: Long, data: String): Boolean {
        return freerdp_send_clipboard_data(inst, data)
    }

    @JvmStatic
    private fun OnConnectionSuccess(inst: Long) {
        listenerLibRdp?.onConnectionSuccess(inst)
        synchronized(lock) {
            mInstanceState.append(inst, true)
            lock.notifyAll()
        }
    }

    @JvmStatic
    private fun OnConnectionFailure(inst: Long) {
        listenerLibRdp?.onConnectionFailure(inst)
        synchronized(lock) {
            mInstanceState.remove(inst)
            lock.notifyAll()
        }
    }

    @JvmStatic
    private fun OnPreConnect(inst: Long) {
        listenerLibRdp?.onPreConnect(inst)
    }

    @JvmStatic
    private fun OnDisconnecting(inst: Long) {
        listenerLibRdp?.onDisconnecting(inst)
    }

    @JvmStatic
    private fun OnDisconnected(inst: Long) {
        listenerLibRdp?.onDisconnected(inst)

        synchronized(lock) {
            mInstanceState.remove(inst)
            lock.notifyAll()
        }
    }

    @JvmStatic
    private fun OnSettingsChanged(inst: Long, width: Int, height: Int, bpp: Int) {
        RdpApp.getSession(inst)?.libRdpUiEventListener?.onSettingsChanged(width, height, bpp)
    }

    @JvmStatic
    private fun OnAuthenticate(
        inst: Long, username: StringBuilder, domain: StringBuilder, password: StringBuilder
    ): Boolean {
        return RdpApp.getSession(inst)
            ?.libRdpUiEventListener
            ?.onAuthenticate(username, domain, password)
            ?: false
    }

    @JvmStatic
    private fun OnGatewayAuthenticate(
        inst: Long, username: StringBuilder, domain: StringBuilder, password: StringBuilder
    ): Boolean {
        return RdpApp.getSession(inst)
            ?.libRdpUiEventListener
            ?.onGatewayAuthenticate(username, domain, password)
            ?: false
    }

    @JvmStatic
    private fun OnVerifyCertificateEx(
        inst: Long,
        host: String?,
        port: Long,
        commonName: String?,
        subject: String?,
        issuer: String?,
        fingerprint: String?,
        flags: Long
    ): Int = RdpApp.getSession(inst.orEmpty())
        ?.libRdpUiEventListener
        ?.onVerifyCertificateEx(
            host.orEmpty(), port.orEmpty(), commonName.orEmpty(),
            subject.orEmpty(), issuer.orEmpty(), fingerprint.orEmpty(), flags.orEmpty()
        ) ?: 0


    @JvmStatic
    private fun OnVerifyChangedCertificateEx(
        inst: Long,
        host: String?,
        port: Long,
        commonName: String?,
        subject: String?,
        issuer: String?,
        fingerprint: String?,
        oldSubject: String?,
        oldIssuer: String?,
        oldFingerprint: String?,
        flags: Long
    ): Int = RdpApp.getSession(inst)
        ?.libRdpUiEventListener
        ?.onVerifyChangedCertificateEx(
            host.orEmpty(), port.orEmpty(), commonName.orEmpty(), subject.orEmpty(),
            issuer.orEmpty(), fingerprint.orEmpty(), oldSubject.orEmpty(), oldIssuer.orEmpty(),
            oldFingerprint.orEmpty(), flags.orEmpty()
        ) ?: 0


    @JvmStatic
    private fun OnGraphicsUpdate(inst: Long, x: Int, y: Int, width: Int, height: Int) {
        RdpApp.getSession(inst)?.libRdpUiEventListener?.onGraphicsUpdate(x, y, width, height)
    }

    @JvmStatic
    private fun OnGraphicsResize(inst: Long, width: Int, height: Int, bpp: Int) {
        RdpApp.getSession(inst)?.libRdpUiEventListener?.onGraphicsResize(width, height, bpp)
    }

    @JvmStatic
    private fun OnRemoteClipboardChanged(inst: Long, data: String?) {
        RdpApp.getSession(inst)?.libRdpUiEventListener?.onRemoteClipboardChanged(data.orEmpty())
    }

    @JvmStatic
    fun version() = freerdp_get_version().orEmpty()

    @JvmStatic
    fun lastError(inst: Long) = freerdp_get_last_error_string(inst).orEmpty()

    init {
        val h264 = "openh264"
        val libraries = arrayOf(
            h264,
            "freerdp-openssl",
            "ssl",
            "crypto",
            "jpeg",
            "winpr3",
            "freerdp3",
            "freerdp-client3",
            "freerdp-android3"
        )
        val libraryPath = System.getProperty("java.library.path")
        libraries.forEach { lib ->
            try {
                System.loadLibrary(lib)
                Log.v(TAG, "尝试从路径: $libraryPath 加载库 $lib 成功")
            } catch (e: UnsatisfiedLinkError) {
                Log.e(TAG, "尝试从路径: $libraryPath 加载库 $lib 失败 ${e.message}")
                if (lib == h264) {
                    mHasH264 = false
                }
            }
        }

        FileUtils.listFilesInDir(PathUtils.getInternalAppFilesPath(), true).forEach {
            if (it.isFile) {
                val cert = FileIOUtils.readFile2String(it)
                Log.e(TAG, it.absolutePath + "  " + cert)
            }
        }
    }

    private fun Long?.orEmpty() = this ?: 0L
}