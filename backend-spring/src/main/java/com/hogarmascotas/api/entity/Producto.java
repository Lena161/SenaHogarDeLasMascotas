package com.hogarmascotas.api.entity;

import com.hogarmascotas.api.exception.ReglaNegocioException;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Producto del inventario.
 *
 * ENCAPSULAMIENTO: el stock NO tiene setter publico; solo los metodos
 * ingresarStock / retirarStock lo modifican, garantizando RNF-08
 * (nunca negativo) y RN-05 desde el propio objeto.
 */
@Entity
@Table(name = "producto")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String categoria;

    @Column(length = 255)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "stock_actual", nullable = false)
    private int stockActual = 0;

    @Column(name = "stock_minimo", nullable = false)
    private int stockMinimo = 5;

    @Column(nullable = false)
    private boolean activo = true;

    // ---------- Comportamiento de dominio ----------

    public void ingresarStock(int cantidad) {
        validarCantidad(cantidad);
        this.stockActual += cantidad;
    }

    public void retirarStock(int cantidad) {
        validarCantidad(cantidad);
        if (cantidad > stockActual) {
            throw new ReglaNegocioException(
                "RN-05: la salida (" + cantidad + ") supera el stock disponible ("
                + stockActual + ") del producto " + nombre + ".");
        }
        this.stockActual -= cantidad;
    }

    public boolean requiereReposicion() {
        return stockActual <= stockMinimo; // RF-13
    }

    private void validarCantidad(int cantidad) {
        if (cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad del movimiento debe ser mayor que cero.");
        }
    }

    // ---------- Getters / setters (sin setStockActual publico) ----------
    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public int getStockActual() { return stockActual; }
    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
