package com.demo.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Profile("local")
public class DebugSecurityContextFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(DebugSecurityContextFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        logger.debug("DebugFilter BEFORE chain - uri={}, auth={}, queryString={}", 
                request.getRequestURI(), 
                SecurityContextHolder.getContext().getAuthentication(),
                request.getQueryString());
        try {
            filterChain.doFilter(request, response);
        } catch (Exception e) {
            logger.debug("Exception in filter chain: {} - {}", e.getClass().getSimpleName(), e.getMessage(), e);
            throw e;
        } finally {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            logger.debug("DebugFilter AFTER chain - uri={}, auth={}, status={}", 
                    request.getRequestURI(), 
                    auth != null ? auth.getAuthorities() : "null",
                    response.getStatus());
        }
    }
}