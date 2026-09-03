package com.hogarmascotas.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Mensaje del formulario publico de contacto.
 * Entidad agregada respecto al modelo de la Fase 2 (documentar en el
 * control de cambios): da paridad con el prototipo Express y permite
 * gestionarlos desde el panel ADMIN.
 */
@Entity
@Table(name = "mensaje_contacto")
public class MensajeContacto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String nombre;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(nullable = false, length = 1000)
    private String mensaje;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false)
    private boolean atendido = false;

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getMensaje() { return mensaje; }
    public void setMensaje(String mensaje) { this.mensaje = mensaje; }
    public LocalDateTime getFecha() { return fecha; }
    public boolean isAtendido() { return atendido; }
    public void setAtendido(boolean atendido) { this.atendido = atendido; }
}
