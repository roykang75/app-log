package xyz.oiio.app_log.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import xyz.oiio.app_log.exception.BaseBusinessException;

@Getter
@AllArgsConstructor
public class ErrorResponse {
    private String errorCode;
    private String message;
    private String traceId;

    public ErrorResponse(BaseBusinessException e) {
        this.errorCode = e.getErrorCode();
        this.message = e.getMessage();
        this.traceId = org.slf4j.MDC.get("trace_id");
    }

    public ErrorResponse(String errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
        this.traceId = org.slf4j.MDC.get("trace_id");
    }
}
