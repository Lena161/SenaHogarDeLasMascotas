package com.hogarmascotas.api.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Emision y validacion de tokens JWT (RNF-03).
 * El token reemplaza a las sesiones de cookie del prototipo Express:
 * es sin estado, por lo que sirve igual a la web y a la app movil.
 */
@Component
public class JwtUtil {

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtUtil(@Value("${app.jwt.secret}") String secreto,
                   @Value("${app.jwt.expiracion-horas}") long horas) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = horas * 60 * 60 * 1000;
    }

    public String generar(String username, String rol) {
        Date ahora = new Date();
        return Jwts.builder()
                .setSubject(username)
                .claim("rol", rol)
                .setIssuedAt(ahora)
                .setExpiration(new Date(ahora.getTime() + expiracionMs))
                .signWith(clave, SignatureAlgorithm.HS256)
                .compact();
    }

    /** Retorna los claims si el token es valido; lanza excepcion si no. */
    public Claims validar(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(clave)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
