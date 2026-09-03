package com.hogarmascotas.api.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtro que corre una vez por peticion: si llega un encabezado
 * "Authorization: Bearer <token>" valido, registra al usuario y su
 * rol en el contexto de seguridad; si no, la peticion sigue anonima
 * y las reglas de SecurityConfig deciden si se permite.
 */
@Component
public class FiltroJwt extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public FiltroJwt(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest peticion,
                                    HttpServletResponse respuesta,
                                    FilterChain cadena) throws ServletException, IOException {
        String encabezado = peticion.getHeader("Authorization");
        if (encabezado != null && encabezado.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtil.validar(encabezado.substring(7));
                String username = claims.getSubject();
                String rol = claims.get("rol", String.class);
                var autenticacion = new UsernamePasswordAuthenticationToken(
                        username, null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + rol)));
                SecurityContextHolder.getContext().setAuthentication(autenticacion);
            } catch (Exception e) {
                // Token invalido o expirado: se continua sin autenticacion;
                // las rutas protegidas responderan 401.
                SecurityContextHolder.clearContext();
            }
        }
        cadena.doFilter(peticion, respuesta);
    }
}
