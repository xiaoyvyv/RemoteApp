package com.freerdp.freerdpcore.services

/**
 * UiEventListener
 */
interface LibRdpUiEventListener {
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

    fun onVerifyCertificate(
        host: String,
        port: Long,
        commonName: String,
        subject: String,
        issuer: String,
        fingerprint: String,
        flags: Long
    ): Int

    fun onVerifyChangedCertificate(
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