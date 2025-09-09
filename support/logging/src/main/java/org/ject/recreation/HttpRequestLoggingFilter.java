package org.ject.recreation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class HttpRequestLoggingFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper;

    private static final int MAX_PAYLOAD_LENGTH = 20_000;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return uri.startsWith("/actuator");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        CustomRequestWrapper wrapped = new CustomRequestWrapper(request);
        logRequest(wrapped);
        filterChain.doFilter(wrapped, response);
    }

    private void logRequest(CustomRequestWrapper request) {
        Map<String, Object> requestLogContext = new LinkedHashMap<>();
        requestLogContext.put("method", request.getMethod());
        requestLogContext.put("uri", request.getRequestURI());
        requestLogContext.put("query", extractQueryParams(request));
        requestLogContext.put("body", extractBody(request));

        try {
            log.info("{}", objectMapper.writeValueAsString(requestLogContext));
        } catch (Exception e) {
            log.info("{}", requestLogContext);
        }
    }

    private Map<String, Object> extractQueryParams(HttpServletRequest request) {
        Map<String, Object> query = new LinkedHashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values == null) {
                return;
            }
            query.put(key, values.length == 1 ? values[0] : Arrays.asList(values));
        });
        return query;
    }

    private Object extractBody(CustomRequestWrapper request) {
        String contentType = Optional.ofNullable(request.getContentType()).orElse("").toLowerCase(Locale.ROOT);
        boolean isJsonContentType = contentType.startsWith(MimeTypeUtils.APPLICATION_JSON_VALUE)
                || contentType.endsWith("+json");

        byte[] body = request.getCachedBody();
        if (body.length == 0) return Collections.emptyMap();

        int loggedLength = Math.min(body.length, MAX_PAYLOAD_LENGTH);
        String requestBodyString = new String(body, 0, loggedLength, StandardCharsets.UTF_8);

        if (!isJsonContentType) {
            return Map.of("raw", requestBodyString, "truncated", body.length > loggedLength);
        }
        try {
            return objectMapper.readValue(requestBodyString, Object.class);
        } catch (Exception e) {
            return Map.of("raw", requestBodyString, "parseError", true);
        }
    }
}
