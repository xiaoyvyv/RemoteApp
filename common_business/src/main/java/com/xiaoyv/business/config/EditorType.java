package com.xiaoyv.business.config;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * SshLoginType
 *
 * @author why
 * @since 2020/12/07
 **/
@IntDef({
        EditorType.TYPE_NONE,
        EditorType.TYPE_SFTP,
        EditorType.TYPE_FTP
})
@Retention(RetentionPolicy.SOURCE)
public @interface EditorType {
    /**
     * 无
     */
    int TYPE_NONE = 0;

    /**
     * SFTP
     */
    int TYPE_SFTP = 1;

    /**
     * FTP
     */
    int TYPE_FTP = 2;
}
