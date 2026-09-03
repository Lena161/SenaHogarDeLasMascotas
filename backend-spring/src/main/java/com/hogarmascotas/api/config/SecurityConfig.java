package com.hogarmascotas.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Reglas de autorizacion (RF-14, RNF-04):
 * - Publico: autenticacion, catalogo de servicios y productos, contacto.
 * - Autenticado: mascotas y citas propias.
 * - Por rol: inventario (ADMIN/RECEPCION), transiciones de cita (staff).
 * Complementadas con @PreAuthorize en los controladores.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final FiltroJwt filtroJwt;

    public SecurityConfig(FiltroJwt filtroJwt) {
        this.filtroJwt = filtroJwt;
    }

    @Bean
    public SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // API sin estado con JWT: no aplica CSRF de cookies
            .cors(cors -> cors.configurationSource(configuracionCors()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/registro", "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/servicios/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/empleados/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/contacto").permitAll()
                .anyRequest().authenticated())
            .addFilterBefore(filtroJwt, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** CORS para el frontend React (dev server de Vite). */
    private CorsConfigurationSource configuracionCors() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:5173"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE"));
        config.setAllowedHeaders(List.of("*"));
        UrlBasedCorsConfigurationSource fuente = new UrlBasedCorsConfigurationSource();
        fuente.registerCorsConfiguration("/**", config);
        return fuente;
    }

    /** BCrypt: mismo algoritmo que ya usaba el prototipo Express (RNF-03). */
    @Bean
    public PasswordEncoder codificador() {
        return new BCryptPasswordEncoder();
    }
}
