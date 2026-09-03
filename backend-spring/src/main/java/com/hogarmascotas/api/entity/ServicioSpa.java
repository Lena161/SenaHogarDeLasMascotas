package com.hogarmascotas.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.math.BigDecimal;

/** Servicio de estetica: recargo por tamano, atendido por esteticistas. */
@Entity
@DiscriminatorValue("SPA")
public class ServicioSpa extends Servicio {

    /** Columna propia del subtipo (modelo Fase 2). */
    @Column(name = "recargo_por_tamano", precision = 10, scale = 2)
    private BigDecimal recargoPorTamano = BigDecimal.ZERO;

    @Override
    public BigDecimal calcularPrecio(Mascota.Tamano tamano) {
        // POLIMORFISMO en accion: bano de un gran danes != bano de un chihuahua.
        // PEQUENO: precio base | MEDIANO: base + recargo | GRANDE: base + 2*recargo
        BigDecimal recargo = recargoPorTamano == null ? BigDecimal.ZERO : recargoPorTamano;
        return switch (tamano) {
            case PEQUENO -> getPrecioBase();
            case MEDIANO -> getPrecioBase().add(recargo);
            case GRANDE  -> getPrecioBase().add(recargo.multiply(BigDecimal.valueOf(2)));
        };
    }

    @Override
    public boolean puedeSerAtendidoPor(Empleado empleado) {
        // RN-04: solo una esteticista puede atender un servicio de spa.
        return empleado instanceof Esteticista;
    }

    public BigDecimal getRecargoPorTamano() { return recargoPorTamano; }
    public void setRecargoPorTamano(BigDecimal recargoPorTamano) { this.recargoPorTamano = recargoPorTamano; }
}
