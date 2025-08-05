package com.charginghive.gateway.security;

import io.jsonwebtoken.Claims;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtils jwtUtils;

    // Define endpoints that are public and should bypass authentication
    private final List<String> openEndpoints = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        log.info("Requested path: {}", path);

        // Allow requests to public endpoints without token verification
        if (openEndpoints.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // Extract Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        log.info("Authorization Header: {}", authHeader);

        // If no Authorization header or malformed, reject the request
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            // Extract and validate JWT token
            String token = authHeader.substring(7);
            Claims claims = jwtUtils.validateToken(token);

            // Extract user identity and roles from token
            String id = claims.getSubject();
            log.info("Authenticated id: {}", id);

            List<String> role = (List<String>) claims.get("authorities");
            log.info("User roles: {}", role);

            // Restrict access to sensitive endpoints (admin-only)
            if ((path.startsWith("/api/admin") || path.equals("/api/auth/get-all"))
                    && !role.contains("ROLE_ADMIN")) {
                log.info("Access denied: ROLE_ADMIN required for this path");
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            if ((path.startsWith("/api/user") || path.equals("/api/auth/edit-user"))
                    && !role.contains("ROLE_DRIVER")) {
                log.info("Access denied: ROLE_DRIVER required for this path");
                exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
                return exchange.getResponse().setComplete();
            }

            // We can add role-based checks for other paths here
            // e.g., OWNER routes, DRIVER routes, etc.

            // Mutate request to pass user details to downstream services (e.g., userId as a header)
            ServerHttpRequest modifiedRequest = exchange.getRequest()
                    .mutate()
                    .header("X-User-Id", id)
                    // .header("X-User-Role", role.toString()) // Uncomment if needed
                    .build();

            // Proceed with the modified request
            return chain.filter(exchange.mutate().request(modifiedRequest).build());

        } catch (Exception ex) {
            // If token is invalid or any exception occurs, respond with UNAUTHORIZED
            log.info("Token validation failed: {}", ex.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    // Ensure this filter has high precedence
    @Override
    public int getOrder() {
        return -1;
    }
}
