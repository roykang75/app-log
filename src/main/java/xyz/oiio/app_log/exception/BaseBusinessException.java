package xyz.oiio.app_log.exception;

import lombok.Getter;

@Getter
public abstract class BaseBusinessException extends RuntimeException {
    private final String errorCode;
    private final int status;

    public BaseBusinessException(String errorCode, String message, int status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }
}
