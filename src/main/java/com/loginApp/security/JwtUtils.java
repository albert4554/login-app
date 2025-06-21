package com.loginApp.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.loginApp.dto.JwtResponseDto;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtUtils {
    private static final String SECRET_KEY = "tu_clave_secreta";
    private final long ACCESS_TOKEN_EXPIRATION = 1000 * 60 * 60 * 2;
    private final long REFRESH_TOKEN_EXPIRATION = 1000 * 60 * 60 * 3;  // 3 minutos
    private static Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);
    // Generar el Access Token
    public String generateAccessToken(String username) {
        String[] rolesArray = getRoles().toArray(new String[0]);
        return JWT.create()
                .withSubject(username)
                .withArrayClaim("roles",rolesArray)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRATION))
                .sign(ALGORITHM);
    }

    // Generar el Refresh Token
    public String generateRefreshToken(String username) {
        String[] rolesArray = getRoles().toArray(new String[0]);
        return JWT.create()
                .withSubject(username)
                .withArrayClaim("roles",rolesArray)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION))
                .sign(ALGORITHM);
    }

    // Extraer el nombre de usuario desde el token
    public String extractUsername(String token) {
        DecodedJWT decodedJWT = JWT.require(ALGORITHM)
                .build()
                .verify(token);
        return decodedJWT.getSubject();
    }

    // Validar el token
    public boolean validateToken(String token) {
      //  return !isTokenExpired(token);
        try {
            JWT.require(ALGORITHM).build().verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    // Verificar si el token está expirado
    private boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        return expirationDate.before(new Date());
    }

    // Extraer la fecha de expiración
    private Date extractExpiration(String token) {
        DecodedJWT decodedJWT = JWT.require(ALGORITHM)
                .build()
                .verify(token);
        return decodedJWT.getExpiresAt();
    }

    public JwtResponseDto refreshToken(String refreshToken){
        if(refreshToken != null && validateToken(refreshToken)){
            String username = extractUsername(refreshToken);
            String newAccessToken =  generateAccessToken(username);
            String newRefreshToken = generateRefreshToken(username);
            return JwtResponseDto
                    .builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken)
                    .build();
        }else{
            throw new AuthenticationCredentialsNotFoundException("Invalid or expired refresh token");
        }
    }
    private List<String> getRoles() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            return Collections.emptyList();
        }

        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();

        return userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }

}
