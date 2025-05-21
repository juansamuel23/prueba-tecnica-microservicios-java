package com.example.productos_service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter{

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    private static final String API_KEY_HEADER = "X-API-Key";

    @Value("${app.security.api-key}")
    private String requiredApiKey; // Renombrado para mayor claridad

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestApiKey = request.getHeader(API_KEY_HEADER);

        // Opcional: Permite pasar requests a /actuator/** sin API Key (para health checks, etc.)
        if (request.getRequestURI().startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (requestApiKey == null || !requestApiKey.equals(requiredApiKey)) {
            logger.warn("Acceso no autorizado a {} desde {}. API Key inválida o faltante.",
                    request.getRequestURI(), request.getRemoteAddr());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json"); // Para devolver JSON
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"API Key inválida o faltante.\"}");
            return;
        }

        logger.debug("API Key válida para la petición a {}", request.getRequestURI());
        filterChain.doFilter(request, response);
    }
}
