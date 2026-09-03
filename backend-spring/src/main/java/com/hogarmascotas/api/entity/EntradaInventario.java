package com.hogarmascotas.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Entrada de mercancia: suma stock. */
@Entity
@DiscriminatorValue("ENTRADA")
public class EntradaInventario extends MovimientoInventario {

    @Column(length = 100)
    private String proveedor;

    @Override
    public void aplicar(Producto producto) {
        producto.ingresarStock(getCantidad());
    }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }
}
