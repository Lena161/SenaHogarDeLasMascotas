package com.hogarmascotas.api.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Subtipo operativo: gestiona citas y registra movimientos de venta. */
@Entity
@DiscriminatorValue("RECEPCION")
public class Recepcionista extends Empleado {
    @Override
    public String getRol() { return "RECEPCION"; }
}
