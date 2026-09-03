package com.hogarmascotas.api.entity;

import jakarta.persistence.*;

/** Subtipo profesional autorizado para servicios de CONSULTA (RN-04). */
@Entity
@DiscriminatorValue("VETERINARIO")
public class Veterinario extends Empleado {

    @Column(name = "tarjeta_profesional", length = 30)
    private String tarjetaProfesional;

    @Column(length = 60)
    private String especialidad;

    @Override
    public String getRol() { return "VETERINARIO"; }

    public String getTarjetaProfesional() { return tarjetaProfesional; }
    public void setTarjetaProfesional(String tarjetaProfesional) { this.tarjetaProfesional = tarjetaProfesional; }
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}
