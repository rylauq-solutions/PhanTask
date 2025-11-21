package com.phantask.authentication.security;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * Utility helper for creating, parsing and validating JSON Web Tokens (JWT).
 *
 * <p>This component centralizes token-related logic such as:
 * <ul>
 *   <li>Generating signed tokens containing user identity and claims</li>
 *   <li>Extracting username or other claims from an existing token</li>
 *   <li>Validating token expiration and signature</li>
 * </ul>
 *
 * <p>Keep secret keys, expiration durations, and token formats configurable (e.g., via application.properties).
 */
@Component
public class JwtUtil {
	
	private final long ACCESS_TOKEN_EXP = 1000 * 60 * 60; //1-hour
    private final long REFRESH_TOKEN_EXP = 1000 * 60 * 60 * 24 * 7; //7-days


    @Value("${jwt.secret}")
    private String SECRET;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes());
    }

	/**
     * Generate a JWT for the given username and claims.
     *
     * @param username the subject (typically username or user id)
     * @return a signed JWT string
     */
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXP))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }
    
    public String generateRefreshToken(UserDetails userDetails) {
        return Jwts.builder()
            .setSubject(userDetails.getUsername())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXP))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

	/**
     * Extract the username (subject) from the JWT.
     *
     * @param token the JWT string
     * @return the username embedded in the token
     */
    public String extractUsername(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getSubject();
    }

	 /**
     * Validate the provided JWT string.
     *
     * @param token the JWT to validate
     * @return true if token is valid (signature and expiration), false otherwise
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        return extractUsername(token).equals(userDetails.getUsername()) && !isExpired(token);
    }

    private boolean isExpired(String token) {
        Date exp = Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody()
            .getExpiration();
        return exp.before(new Date());
    }
}


