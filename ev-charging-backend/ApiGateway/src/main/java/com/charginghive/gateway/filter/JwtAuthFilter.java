package com.charginghive.gateway.filter;

import com.charginghive.gateway.utils.JwtUtils;
import com.charginghive.gateway.utils.RouterValidator;
import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;
    private final RouterValidator routerValidator;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        if (routerValidator.isSecured.test(request)) {

            // 1. Check for missing Authorization header
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                log.warn("Missing Authorization header on secured endpoint: {}", request.getURI());
                return onError(exchange, "Authorization header missing", HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization header format", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(7);
            Claims claims;

            try {
                claims = jwtUtils.validateToken(token);
            } catch (Exception e) {
                log.error("JWT validation failed: {}", e.getMessage());
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            // 2. Extract user roles and check access
            List<String> roles = (List<String>) claims.get("authorities");
            if (!hasRequiredRole(request, roles)) {
                log.warn("Insufficient role to access {}. Roles: {}", request.getURI().getPath(), roles);
                return onError(exchange, "Access denied: insufficient permissions", HttpStatus.FORBIDDEN);
            }

            // 3. Add user ID header
            String userId = claims.getSubject();
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header("X-User-Id", userId)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        }

        // Allow public endpoints
        return chain.filter(exchange);
    }

    private boolean hasRequiredRole(ServerHttpRequest request, List<String> roles) {
        String path = request.getURI().getPath();

        if (roles == null || roles.isEmpty()) return false;

        if (path.startsWith("/api/admin")) {
            return roles.contains("ROLE_ADMIN");
        }

        if (path.startsWith("/api/station")) {
            if (path.contains("/unapproved")) {
                return roles.contains("ROLE_ADMIN");
            }

            if (request.getMethod() != HttpMethod.GET) {
                return roles.contains("ROLE_ADMIN") || roles.contains("ROLE_OPERATOR");
            }
        }

        if (path.equals("/api/auth/get-all")) {
            return roles.contains("ROLE_ADMIN");
        }

        if (path.equals("/api/auth/edit-user")) {
            return roles.contains("ROLE_DRIVER");
        }

        // Default to allowing access if no specific path rule matched
        return true;
    }

    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage, HttpStatus status) {
        log.warn("Request blocked: {} - {}", status, errorMessage);
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        return response.setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
