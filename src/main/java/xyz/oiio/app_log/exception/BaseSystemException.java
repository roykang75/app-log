package xyz.oiio.app_log.exception;

public abstract class BaseSystemException extends RuntimeException {
    public BaseSystemException(String message) {
        super(message);
    }

    public BaseSystemException(String message, Throwable cause) {
        super(message, cause);
    }
}
