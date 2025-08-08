package com.charginghive.gateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
@Slf4j
public class JwtUtils{
    @Value("${jwt.secret.key}")
    private String SECRET;

    @Value("${jwt.expiration.time}")
    private int jwtExpiration;

    private SecretKey keys;

    @PostConstruct
    public void init(){
        log.info("Key {} Exp Time {}", SECRET, jwtExpiration);
        keys =  Keys.hmacShaKeyFor(SECRET.getBytes());
    }


    public Claims validateToken(String token){
        log.info("--------------inside validate token !-------------");
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(SECRET.getBytes()))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}