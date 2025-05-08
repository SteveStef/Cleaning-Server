package com.mainlineclean.app.aop;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class ControllerLoggingAspect {

    @Around("within(com.mainlineclean.app.controller..*)")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        assert attrs != null;
        HttpServletRequest  request  = attrs.getRequest();
        HttpServletResponse response = attrs.getResponse();

        String httpMethod = request.getMethod();
        String uri        = request.getRequestURI();
        String query      = request.getQueryString();
        String sig        = pjp.getSignature().toShortString();

        log.info("→ Enter {} {}{}  (handler={})",
                httpMethod,
                uri,
                (query != null ? "?" + query : ""),
                sig);

        Object result = pjp.proceed();

        int status;
        if (result instanceof ResponseEntity<?> resp) {
            status = resp.getStatusCode().value();
        } else {
            status = (response != null ? response.getStatus() : -1);
        }

        log.info("← Exit  {} {}{}  → status={}",
                httpMethod,
                uri,
                (query != null ? "?" + query : ""),
                status);

        return result;
    }
}
