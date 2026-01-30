package xyz.oiio.app_log.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class MdcLoggingFilter implements Filter {

    private static final String TRACE_ID = "trace_id";
    private static final String USER_ID = "user_id"; // Internal MDC key
    private static final String HEADER_USER_ID = "X-User-Id"; // Header key

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            // 1. Trace ID
            String traceId = UUID.randomUUID().toString();
            MDC.put(TRACE_ID, traceId);

            // 2. User ID (Simulated extraction)
            if (request instanceof HttpServletRequest httpRequest) {
                String userId = httpRequest.getHeader(HEADER_USER_ID);
                if (userId != null) {
                    MDC.put(USER_ID, userId);
                }
            }

            // 3. Add Trace ID to response header
            if (response instanceof HttpServletResponse httpResponse) {
                httpResponse.setHeader("X-Trace-Id", traceId);
            }

            chain.doFilter(request, response);

        } finally {
            MDC.clear();
        }
    }
}
