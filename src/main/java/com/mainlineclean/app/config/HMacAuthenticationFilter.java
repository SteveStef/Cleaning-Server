package com.mainlineclean.app.config;

import com.mainlineclean.app.utils.HMacSigner;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.List;

@Slf4j
@Configuration
public class HMacAuthenticationFilter extends OncePerRequestFilter {
    private final HMacSigner HMacSigner;
    private final RateLimiterRegistry rateLimiterRegistry;

    public HMacAuthenticationFilter(HMacSigner HMacSigner, RateLimiterRegistry rateLimiterRegistry) {
        this.HMacSigner = HMacSigner;
        this.rateLimiterRegistry = rateLimiterRegistry;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring("Bearer ".length());
        boolean isAdmin = HMacSigner.verify(token, true);
        boolean isUser = !isAdmin && HMacSigner.verify(token, false);

        if (!isAdmin && !isUser) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String grantedRole = isAdmin ? "ROLE_ADMIN" : "ROLE_USER"; // this needs to be this ROLE_SOMETHING (can't customize this)
        Authentication authentication = new UsernamePasswordAuthenticationToken(token, null, List.of(new SimpleGrantedAuthority(grantedRole)));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // add rate limits
        String path = request.getServletPath();
        String method = request.getMethod();
        String bucketKey = token + method + path; // POST/review

        Bucket bucket = rateLimiterRegistry.resolveBucket(bucketKey);
        if(!bucket.tryConsume(1)) {
            log.warn("Rate limit exceeded for {}", bucketKey);
            response.setStatus(429);
            response.getWriter().write("Rate limit exceeded");
            return;
        }

        filterChain.doFilter(request, response);
    }

    // verify has its own type of auth
    // /token anyone can use
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return "/health".equals(request.getServletPath()) || "/verify-code".equals(request.getServletPath()) || "/token".equals(request.getServletPath());
    }
}
