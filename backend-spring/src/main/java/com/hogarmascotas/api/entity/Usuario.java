package com.hogarmascotas.api.entity;

import jakarta.persistence.*;

/** Cuenta de acceso con rol (RF-14). Vinculada a empleado O dueno. */
@Entity
@Table(name = "usuario")
public class Usuario {

    public enum Rol { ADMIN, RECEPCION, VETERINARIO, ESTETICISTA, CLIENTE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 60)
    private String username;

    /** Hash BCrypt, nunca texto plano (RNF-03). */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private Rol rol;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dueno_id")
    private Dueno dueno;

    @Column(nullable = false)
    private boolean activo = true;

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
    public Empleado getEmpleado() { return empleado; }
    public void setEmpleado(Empleado empleado) { this.empleado = empleado; }
    public Dueno getDueno() { return dueno; }
    public void setDueno(Dueno dueno) { this.dueno = dueno; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
