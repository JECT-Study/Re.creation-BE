package org.ject.recreation;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

@Slf4j
@Aspect
@Component
@Profile({"local", "dev"})
public class HttpRequestLoggingAspect {

    private final ObjectMapper objectMapper = new ObjectMapper();

    // Pointcut: 모든 컨트롤러 메서드에 적용
    @Pointcut("execution(* org.ject.recreation..controller..*(..))")
    public void controllerMethods() {}

    // Pointcut: HTTP 요청을 처리하는 어노테이션이 붙은 메서드에만 적용
    @Pointcut("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public void httpRequestMappingMethods() {}

    @Around("controllerMethods() && httpRequestMappingMethods()")
    public Object logHttpRequestDetails(ProceedingJoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = getCurrentHttpRequest();
        if (request == null) {
            return joinPoint.proceed();
        }

        Map<String, Object> requestLogContext = new LinkedHashMap<>();

        requestLogContext.put("method", request.getMethod());
        requestLogContext.put("uri", request.getRequestURI());
        requestLogContext.put("query", extractQueryParams(request));
        requestLogContext.put("body", extractRequestBody(joinPoint));

        log.debug("{}", objectMapper.writeValueAsString(requestLogContext));

        return joinPoint.proceed();
    }

    private HttpServletRequest getCurrentHttpRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return (attributes != null) ? attributes.getRequest() : null;
    }

    private Map<String, Object> extractQueryParams(HttpServletRequest request) {
        Map<String, Object> query = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            query.put(key, values.length == 1 ? values[0] : Arrays.asList(values));
        });
        return query;
    }

    private Object extractRequestBody(JoinPoint joinPoint) {
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Object[] args = joinPoint.getArgs();
        Annotation[][] annotations = method.getParameterAnnotations();

        for (int i = 0; i < annotations.length; i++) {
            boolean hasRequestBody = Arrays.stream(annotations[i])
                    .anyMatch(annotation -> annotation instanceof RequestBody);

            if (hasRequestBody) {
                return args[i];
            }
        }

        return Collections.emptyMap();
    }
}

