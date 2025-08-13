package com.charginghive.gateway.utils;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Predicate;

@Component
public class RouterValidator{


    //all public endpoints that do not require a JWT for access
    public static final List<String> openApiEndpoints = List.of(
            "/api/auth/login",
            "/api/auth/register"
    );

    // A interface with sam(boolean test(T t)) to test if a given request is for a secured endpoint
    public Predicate<ServerHttpRequest> isSecured =
            request -> openApiEndpoints
                    .stream()
                    .noneMatch(uri -> request.getURI().getPath().contains(uri));

}