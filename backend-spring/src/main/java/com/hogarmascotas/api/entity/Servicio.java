package com.hogarmascotas.api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Jerarquia de servicios (Fase 1, seccion 4.2).
 *
 * EVIDENCIA POO - HERENCIA: SINGLE_TABLE sobre la tabla `servicio`
 * con la columna `tipo` como discriminador (CONSULTA / SPA),
 * alineada con el modelo fisico de la Fase 2.
 *
 * EVIDENCIA POO - POLIMORFISMO: calcularPrecio() y
 * puedeSerAtendidoPor() se resuelven segun el subtipo real,
 * sin que el codigo cliente pregunte "que tipo eres".
 */
@Entity
@Table(name = "servicio")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public abstract class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "duracion_minutos", nullable = false)
    private int duracionMinutos = 30;

    @Column(name = "precio_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioBase;

    @Column(nullable = false)
    private boolean activo = true;

    /**
     * POLIMORFISMO: cada subtipo define como se calcula su precio
     * segun el tamano de la mascota.
     */
    public abstract BigDecimal calcularPrecio(Mascota.Tamano tamano);

    /**
     * POLIMORFISMO al servicio de la regla RN-04: cada subtipo sabe
     * que perfil profesional puede atenderlo. La capa de servicio
     * solo pregunta al objeto, no hace if por tipo.
     */
    public abstract boolean puedeSerAtendidoPor(Empleado empleado);

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public int getDuracionMinutos() { return duracionMinutos; }
    public void setDuracionMinutos(int duracionMinutos) { this.duracionMinutos = duracionMinutos; }
    public BigDecimal getPrecioBase() { return precioBase; }
    public void setPrecioBase(BigDecimal precioBase) { this.precioBase = precioBase; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
