package com.mainlineclean.app.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class ControllerLoggingAspect {

    @Around("within(com.mainlineclean.app.controller..*)")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        // grab the current RequestAttributes (may be null)
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();

        // bail out if we're not in an HTTP servlet request
        if (!(attrs instanceof ServletRequestAttributes)) {
            return pjp.proceed();
        }

        ServletRequestAttributes servletAttrs = (ServletRequestAttributes) attrs;
        HttpServletRequest request = servletAttrs.getRequest();  // now safe

        // -- your existing exclusion logic, e.g. skip GET /availability --
        if ("GET".equals(request.getMethod()) && "/availability".equals(request.getRequestURI())) {
            return pjp.proceed();
        }

        String method = request.getMethod();
        String uri    = request.getRequestURI();
        String query  = request.getQueryString();
        String sig    = pjp.getSignature().toShortString();

        log.info("{} {}{}  (handler={})", method, uri, (query != null ? "?" + query : ""), sig);

        return pjp.proceed();
    }
}
