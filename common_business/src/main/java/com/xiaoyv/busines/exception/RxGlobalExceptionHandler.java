package com.xiaoyv.busines.exception;

/**
 * AppRxExceptionHandler
 *
 * @author why
 * @since 2020/11/29
 **/
public class RxGlobalExceptionHandler implements IExceptionHandler {
    @Override
    public RxException handleException(Throwable e) {
        return new RxException(e, RxException.DEFAULT_ERROR);
    }
}
