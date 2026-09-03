package com.hogarmascotas.api.entity;

import jakarta.persistence.*;

/** Subtipo profesional autorizado para servicios de SPA (RN-04). */
@Entity
@DiscriminatorValue("ESTETICISTA")
public class Esteticista extends Empleado {

    @Column(length = 60)
    private String certificacion;

    @Override
    public String getRol() { return "ESTETICISTA"; }

    public String getCertificacion() { return certificacion; }
    public void setCertificacion(String certificacion) { this.certificacion = certificacion; }
}
