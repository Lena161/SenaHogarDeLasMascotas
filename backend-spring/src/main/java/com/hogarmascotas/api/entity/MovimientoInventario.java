package com.hogarmascotas.api.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Jerarquia de movimientos de inventario (Fase 1, seccion 4.2).
 *
 * HERENCIA SINGLE_TABLE sobre `movimiento_inventario`, columna `tipo`
 * como discriminador (ENTRADA / SALIDA).
 *
 * POLIMORFISMO: aplicar(producto) tiene un efecto distinto por
 * subtipo; el servicio de inventario lo invoca sin condicionales.
 */
@Entity
@Table(name = "movimiento_inventario")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo", discriminatorType = DiscriminatorType.STRING)
public abstract class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // responsable del movimiento (RNF-10)

    @Column(nullable = false)
    private int cantidad;

    @Column(nullable = false, length = 150)
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    /** POLIMORFISMO: cada subtipo define su efecto sobre el stock. */
    public abstract void aplicar(Producto producto);

    public Long getId() { return id; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }
    public LocalDateTime getFecha() { return fecha; }
}
