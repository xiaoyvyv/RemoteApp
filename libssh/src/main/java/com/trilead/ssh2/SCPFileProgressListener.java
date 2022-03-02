package com.trilead.ssh2;

/**
 * SPCListener
 *
 * @author why
 * @since 2022/3/2
 */
public interface SCPFileProgressListener {
    void onProgress(String srcFileName, String targetFileName, Long current, Long total);
}
