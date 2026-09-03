package com.hogarmascotas.api.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/** Subtipo administrativo: gestiona catalogo, inventario y personal. */
@Entity
@DiscriminatorValue("ADMIN")
public class Administrador extends Empleado {
    @Override
    public String getRol() { return "ADMIN"; }
}
