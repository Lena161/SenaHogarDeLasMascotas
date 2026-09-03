package com.hogarmascotas.api.entity;

import jakarta.persistence.*;

/** Salida de mercancia: valida disponibilidad (RN-05) y resta stock. */
@Entity
@DiscriminatorValue("SALIDA")
public class SalidaInventario extends MovimientoInventario {

    public enum Destino { VENTA, CONSUMO_INTERNO, BAJA }

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Destino destino;

    @Override
    public void aplicar(Producto producto) {
        // La validacion RN-05 vive en Producto.retirarStock():
        // el objeto producto protege su propio invariante.
        producto.retirarStock(getCantidad());
    }

    public Destino getDestino() { return destino; }
    public void setDestino(Destino destino) { this.destino = destino; }
}
