package xyz.oiio.app_log.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import xyz.oiio.app_log.dto.ErrorResponse;
import xyz.oiio.app_log.exception.BaseBusinessException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. Business Exception (No Code Fix Required)
    @ExceptionHandler(BaseBusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BaseBusinessException e) {
        try {
            org.slf4j.MDC.put("exception_category", "BUSINESS");
            log.warn("[BUSINESS] Code: {}, Message: {}", e.getErrorCode(), e.getMessage());
        } finally {
            org.slf4j.MDC.remove("exception_category");
        }
        return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e));
    }

    // 2. System Exception & General RuntimeException (Potential Code Fix Required)
    @ExceptionHandler({ RuntimeException.class, Exception.class })
    public ResponseEntity<ErrorResponse> handleSystemException(Exception e) {
        try {
            org.slf4j.MDC.put("exception_category", "SYSTEM");
            org.slf4j.MDC.put("is_fix_required", "true");
            // Special marker or field for auto-fix system to catch
            log.error("[SYSTEM_ERROR] Message: {}, StackTrace: ", e.getMessage(), e);
        } finally {
            org.slf4j.MDC.remove("exception_category");
            org.slf4j.MDC.remove("is_fix_required");
        }
        return ResponseEntity.status(500)
                .body(new ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected system error occurred."));
    }
}
