package com.trilead.ssh2.auth;

public interface AgentIdentity {
    String getAlgName();

    byte[] getPublicKeyBlob();

    byte[] sign(byte[] data);
}
