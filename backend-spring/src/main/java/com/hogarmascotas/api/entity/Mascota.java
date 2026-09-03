package com.hogarmascotas.api.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Period;

/** Paciente de la veterinaria. Pertenece a un unico dueno (RN-03). */
@Entity
@Table(name = "mascota")
public class Mascota {

    public enum Tamano { PEQUENO, MEDIANO, GRANDE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dueno_id", nullable = false)
    private Dueno dueno;

    @Column(nullable = false, length = 60)
    private String nombre;

    @Column(nullable = false, length = 20)
    private String especie;

    @Column(length = 60)
    private String raza;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(nullable = false, length = 1)
    private String sexo;

    @Column(name = "peso_kg")
    private Double pesoKg;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Tamano tamano = Tamano.MEDIANO;

    @Column(nullable = false)
    private boolean activo = true;

    /** Comportamiento de dominio: edad calculada, no almacenada. */
    public int calcularEdad() {
        if (fechaNacimiento == null) return 0;
        return Period.between(fechaNacimiento, LocalDate.now()).getYears();
    }

    public Long getId() { return id; }
    public Dueno getDueno() { return dueno; }
    public void setDueno(Dueno dueno) { this.dueno = dueno; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEspecie() { return especie; }
    public void setEspecie(String especie) { this.especie = especie; }
    public String getRaza() { return raza; }
    public void setRaza(String raza) { this.raza = raza; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
    public String getSexo() { return sexo; }
    public void setSexo(String sexo) { this.sexo = sexo; }
    public Double getPesoKg() { return pesoKg; }
    public void setPesoKg(Double pesoKg) { this.pesoKg = pesoKg; }
    public Tamano getTamano() { return tamano; }
    public void setTamano(Tamano tamano) { this.tamano = tamano; }
    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }
}
