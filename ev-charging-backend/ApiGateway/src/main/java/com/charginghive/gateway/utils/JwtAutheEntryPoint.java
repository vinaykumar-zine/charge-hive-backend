package com.charginghive.gateway.utils;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component //spring bean
public class JwtAutheEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException, java.io.IOException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                "You are unauthorized to access this endpoint!!!!!!!!!");//SC 401 + error mesg

    }

}