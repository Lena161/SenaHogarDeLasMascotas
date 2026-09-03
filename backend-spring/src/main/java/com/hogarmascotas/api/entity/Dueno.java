package com.hogarmascotas.api.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/** Cliente propietario de mascotas. Hereda la identidad de Persona. */
@Entity
@Table(name = "dueno")
public class Dueno extends Persona {

    @Column(length = 150)
    private String direccion;

    @Column(nullable = false)
    private boolean activo = true;

    /** RN-03: un dueno posee una o varias mascotas. */
    @OneToMany(mappedBy = "dueno", fetch = FetchType.LAZY)
    private List<Mascota> mascotas = new ArrayList<>();

    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
    public List<Mascota> getMascotas() { return mascotas; }
}
