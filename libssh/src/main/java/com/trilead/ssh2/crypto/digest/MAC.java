package com.trilead.ssh2.crypto.digest;

/**
 * MAC. Replace by {@link MessageMac to support enchanced Mac algorithms}
 *
 * @author Christian Plattner, plattner@trilead.com
 * @version $Id: MAC.java,v 1.1 2007/10/15 12:49:57 cplattne Exp $
 */
/* This class is technically deprecated, but isn't marked as such since it's
 * just the implementation with the public fields that's deprecated, rather than
 * any of the methods in it
 */
public class MAC {
    MessageMac mac;

    int size;

    /**
     * @param type the MAC algorithm to use
     * @param key  the key to use in the MAC
     */
    public MAC(String type, byte[] key) {
        mac = new MessageMac(type, key);

        size = mac.size();
    }

    public void initMac(int seq) {
        mac.initMac(seq);
    }

    public void update(byte[] packetdata, int off, int len) {
        mac.update(packetdata, off, len);
    }

    public void getMac(byte[] out, int off) {
        mac.getMac(out, off);
    }

    public int size() {
        return size;
    }
}
