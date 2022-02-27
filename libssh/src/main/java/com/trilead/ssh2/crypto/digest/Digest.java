package com.trilead.ssh2.crypto.digest;

/**
 * Digest.
 *
 * @author Christian Plattner, plattner@trilead.com
 * @version $Id: Digest.java,v 1.1 2007/10/15 12:49:57 cplattne Exp $
 */
public interface Digest {
    int getDigestLength();

    void update(byte b);

    void update(byte[] b);

    void update(byte[] b, int off, int len);

    void reset();

    void digest(byte[] out);

    void digest(byte[] out, int off);
}
