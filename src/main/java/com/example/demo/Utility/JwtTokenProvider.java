package com.example.demo.Utility;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String accessTokenSecret;

    @Value("${jwt.refresh.secret}")
    private String refreshTokenSecret;

    @Value("${jwt.expiration}")
    private long accessTokenExpiration; // 15 minutes

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration; // 7 days

    // Extract username from token
    public String getUsernameFromToken(String token, boolean isAccessToken) {
        return getClaimsFromToken(token, Claims::getSubject, isAccessToken);
    }

    // Extract expiration date from token
    public Date getExpirationDateFromToken(String token, boolean isAccessToken) {
        return getClaimsFromToken(token, Claims::getExpiration, isAccessToken);
    }

    // Generic method to extract claims
    private <T> T getClaimsFromToken(String token, Function<Claims, T> claimsResolver, boolean isAccessToken) {
        final Claims claims = getAllClaims(token, isAccessToken);
        return claimsResolver.apply(claims);
    }

    // Extract all claims
    private Claims getAllClaims(String token, boolean isAccessToken) {
        return Jwts.parserBuilder()
                .setSigningKey(isAccessToken ? getAccessSignKey() : getRefreshSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if token is expired
    private boolean isTokenExpired(String token, boolean isAccessToken) {
        final Date expirationDate = getExpirationDateFromToken(token, isAccessToken);
        return expirationDate.before(new Date());
    }

    // Generate access token
    public String generateAccessToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", "USER");
        return createToken(username, claims, true);
    }

    // Generate refresh token
    public String generateRefreshToken(String username) {
        return createToken(username, new HashMap<>(), false);
    }

    // Create a token with the correct secret and expiration
    private String createToken(String username, Map<String, Object> claims, boolean isAccessToken) {
        long expirationTime = isAccessToken ? accessTokenExpiration : refreshTokenExpiration;
        Key signingKey = isAccessToken ? getAccessSignKey() : getRefreshSignKey();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    // Decode access token secret key
    private Key getAccessSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(accessTokenSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Decode refresh token secret key
    private Key getRefreshSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(refreshTokenSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Validate token
    public boolean validateToken(String token, UserDetails userDetails, boolean isAccessToken) {
        final String usernameFromToken = getUsernameFromToken(token, isAccessToken);
        return (usernameFromToken.equals(userDetails.getUsername()) && !isTokenExpired(token, isAccessToken));
    }

    // Validate refresh token and return claims
    public Claims validateRefreshToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getRefreshSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Validate access token
    public boolean validateAccessToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getAccessSignKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
