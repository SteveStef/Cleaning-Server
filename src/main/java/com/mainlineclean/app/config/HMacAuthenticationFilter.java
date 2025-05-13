package com.mainlineclean.app.config;

import com.mainlineclean.app.utils.HMacSigner;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.Authentication;

import java.io.IOException;
import java.util.List;

@Configuration
public class HMacAuthenticationFilter extends OncePerRequestFilter {
    private final HMacSigner HMacSigner;

    public HMacAuthenticationFilter(HMacSigner HMacSigner) {
        this.HMacSigner = HMacSigner;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String auth = request.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String token = auth.substring(7);
        boolean isAdmin = HMacSigner.verify(token, true);
        boolean isUser = !isAdmin && HMacSigner.verify(token, false);

        if (!isAdmin && !isUser) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String grantedRole = isAdmin ? "ROLE_ADMIN" : "ROLE_USER";
        Authentication authentication = new UsernamePasswordAuthenticationToken(token, null, List.of(new SimpleGrantedAuthority(grantedRole)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return "/verify-code".equals(request.getServletPath()) || "/token".equals(request.getServletPath());
    }
}