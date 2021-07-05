package com.freerdp.freerdpcore.services

/**
 * UiEventListener
 */
interface UiEventListener {
    fun onSettingsChanged(width: Int, height: Int, bpp: Int)

    fun onAuthenticate(
        username: StringBuilder,
        domain: StringBuilder,
        password: StringBuilder
    ): Boolean

    fun onGatewayAuthenticate(
        username: StringBuilder,
        domain: StringBuilder,
        password: StringBuilder
    ): Boolean

    fun onVerifyCertificateEx(
        host: String,
        port: Int,
        commonName: String,
        subject: String,
        issuer: String,
        fingerprint: String,
        flags: Long
    ): Int

    fun onVerifyChangedCertificateEx(
        host: String,
        port: Long,
        commonName: String,
        subject: String,
        issuer: String,
        fingerprint: String,
        oldSubject: String,
        oldIssuer: String,
        oldFingerprint: String,
        flags: Long
    ): Int

    fun onGraphicsUpdate(
        x: Int,
        y: Int,
        width: Int,
        height: Int
    )

    fun onGraphicsResize(
        width: Int,
        height: Int,
        bpp: Int
    )

    fun onRemoteClipboardChanged(data: String)
}