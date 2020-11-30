package com.xiaoyv.busines.exception;

/**
 * RxException
 *
 * @author why
 * @since 2020/11/11
 */
public class RxException extends Exception {
    private static final long serialVersionUID = 5511484461615429825L;

    /**
     * 默认错误码
     */
    public static final int DEFAULT_ERROR = -1;

    /**
     * 自定义的错误码
     */
    private final int code;

    public RxException(String message) {
        super(message);
        this.code = DEFAULT_ERROR;
    }

    public RxException(String message, int code) {
        super(message);
        this.code = code;
    }

    public RxException(Throwable e, int code) {
        super(e);
        this.code = code;
    }

    /**
     * 获取自定义的错误码
     *
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取详情信息
     *
     * @return 详情信息
     */
    @Override
    public String getMessage() {
        return super.getMessage();
    }
}