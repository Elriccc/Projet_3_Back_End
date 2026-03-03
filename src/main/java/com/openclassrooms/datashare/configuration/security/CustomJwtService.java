package com.openclassrooms.datashare.configuration.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;

@Service
public class CustomJwtService {

    @Value("${jwt.secret}")
    private String JWT_SECRET;
    @Value("${jwt.expiration}")
    private int JWT_EXPIRATION_IN_MS;

    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .claims(new HashMap<>())
                .subject(userDetails.getUsername())
                .issuedAt(new Date())
                .expiration(new Date((new Date()).getTime() + JWT_EXPIRATION_IN_MS))
                .signWith(this.getSigningKey())
                .compact();
    }

    public String getUsernameFromToken(String token) {
        if(this.isTokenExpired(token)){
            return null;
        }
        try {
            return this.extractAllClaims(token).getSubject();
        } catch(ExpiredJwtException exception){
            return null;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String USERNAME = this.getUsernameFromToken(token);
        return USERNAME.equals(userDetails.getUsername()) && !this.isTokenExpired(token);
    }

    private SecretKey getSigningKey(){
        return Keys.hmacShaKeyFor(JWT_SECRET.getBytes());
    }

    public boolean isTokenExpired(String token) {
        try {
            return this.extractAllClaims(token).getExpiration().before(new Date());
        } catch (ExpiredJwtException exception){
            return true;
        }
    }

    private Claims extractAllClaims(String token) throws ExpiredJwtException{
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
