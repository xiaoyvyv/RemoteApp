package com.xiaoyv.business.bean.ssh;

import java.io.Serializable;

/**
 * KeyCodeBean
 *
 * @author why
 * @since 2020/12/08
 **/
public class KeyCodeBean implements Serializable {
    private static final long serialVersionUID = -3794747244145246446L;
    private int key;
    private String value;

    public KeyCodeBean(int key, String value) {
        this.key = key;
        this.value = value;
    }

    public int getKey() {
        return key;
    }

    public String getValue() {
        return value == null ? "" : value;
    }
}
