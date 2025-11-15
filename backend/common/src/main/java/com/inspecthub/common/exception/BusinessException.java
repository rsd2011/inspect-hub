package com.inspecthub.common.exception;

import lombok.Getter;

/**
 * 비즈니스 예외
 *
 * 애플리케이션 비즈니스 로직에서 발생하는 예외
 */
@Getter
public class BusinessException extends RuntimeException {

    private final String errorCode;

    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
}
