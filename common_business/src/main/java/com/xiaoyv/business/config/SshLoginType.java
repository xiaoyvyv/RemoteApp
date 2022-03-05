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
@IntDef({SshLoginType.TYPE_NONE, SshLoginType.TYPE_PASSWORD, SshLoginType.TYPE_PUBLIC_KEY})
@Retention(RetentionPolicy.SOURCE)
public @interface SshLoginType {
    /**
     * 无
     */
    int TYPE_NONE = 0;
    /**
     * 密码
     */
    int TYPE_PASSWORD = 1;
    /**
     * 秘钥
     */
    int TYPE_PUBLIC_KEY = 2;
}
