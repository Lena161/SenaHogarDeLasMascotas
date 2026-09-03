package com.hogarmascotas.api.entity;

import jakarta.persistence.*;

/**
 * Superclase ABSTRACTA de la jerarquia de personas (Fase 1, seccion 4.2).
 *
 * EVIDENCIA POO - HERENCIA: Dueno y Empleado comparten identidad
 * (documento, nombres, contacto) sin duplicar atributos.
 *
 * Se usa @MappedSuperclass (y no una tabla "persona") porque el modelo
 * fisico de la Fase 2 definio tablas separadas `dueno` y `empleado`:
 * la herencia existe en el codigo, y cada subclase persiste en su tabla.
 *
 * EVIDENCIA POO - ENCAPSULAMIENTO: todos los atributos son privados;
 * el acceso es via getters/setters que pueden validar.
 */
@MappedSuperclass
public abstract class Persona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tipo_documento", nullable = false, length = 10)
    private String tipoDocumento = "CC";

    @Column(name = "numero_documento", nullable = false, length = 20)
    private String numeroDocumento;

    @Column(nullable = false, length = 80)
    private String nombres;

    @Column(nullable = false, length = 80)
    private String apellidos;

    @Column(nullable = false, length = 20)
    private String telefono;

    @Column(nullable = false, length = 120)
    private String correo;

    /** Comportamiento comun heredado por todas las personas del dominio. */
    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    // --- Getters y setters (encapsulamiento) ---
    public Long getId() { return id; }
    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { this.tipoDocumento = tipoDocumento; }
    public String getNumeroDocumento() { return numeroDocumento; }
    public void setNumeroDocumento(String numeroDocumento) { this.numeroDocumento = numeroDocumento; }
    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }
    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
}
