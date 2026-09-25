package com.tms.common.exception;

import com.tms.common.enums.ErrorCode;

public class TmsException extends RuntimeException {

    private final ErrorCode errorCode;

    public TmsException(ErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
    }

    public TmsException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
