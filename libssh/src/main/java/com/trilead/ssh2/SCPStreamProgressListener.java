package com.trilead.ssh2;

/**
 * SPCListener
 *
 * @author why
 * @since 2022/3/2
 */
public interface SCPStreamProgressListener {

    void onProgress(String srcFileName, Long current, Long total);
}
