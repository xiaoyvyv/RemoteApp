package com.xiaoyv.rdp.screen.listener;

import com.xiaoyv.librdp.jni.LibFreeRDP;

/**
 * RdpUiChangeListener
 *
 * @author why
 * @since 2020/12/02
 **/
public class RdpUiEventListener implements LibFreeRDP.UIEventListener {

    @Override
    public void OnSettingsChanged(int width, int height, int bpp) {

    }

    @Override
    public boolean OnAuthenticate(StringBuilder username, StringBuilder domain, StringBuilder password) {
        return false;
    }

    @Override
    public boolean OnGatewayAuthenticate(StringBuilder username, StringBuilder domain, StringBuilder password) {
        return false;
    }

    @Override
    public int OnVerifyCertificate(String commonName, String subject, String issuer, String fingerprint, boolean mismatch) {
        return 0;
    }

    @Override
    public int OnVerifyChangedCertificate(String commonName, String subject, String issuer, String fingerprint, String oldSubject, String oldIssuer, String oldFingerprint) {
        return 0;
    }

    @Override
    public void OnGraphicsUpdate(int x, int y, int width, int height) {

    }

    @Override
    public void OnGraphicsResize(int width, int height, int bpp) {

    }

    @Override
    public void OnRemoteClipboardChanged(String data) {

    }
}
