package com.hogarmascotas.api.entity;

import com.hogarmascotas.api.exception.ReglaNegocioException;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Registro clinico de una atencion (RF-09, RF-10).
 *
 * DECISION DE ARQUITECTURA (revision de la Fase 1): la Fase 1 propuso
 * alojar el historial en una base documental. Al implementarlo se
 * verifico que el registro tiene estructura fija y conocida, volumen
 * bajo y una relacion fuerte con mascota, cita y veterinario, de modo
 * que separarlo en otro motor obligaria a resolver la integridad
 * referencial en codigo sin ninguna ganancia. Se implementa relacional.
 *
 * ENCAPSULAMIENTO: el historial es inmutable una vez creado salvo las
 * observaciones. No hay setters de diagnostico ni tratamiento: una
 * historia clinica se corrige agregando un registro, nunca reescribiendo
 * el anterior (trazabilidad, RNF-10).
 */
@Entity
@Table(name = "historial_clinico")
public class HistorialClinico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;

    /** La cita que origino la atencion. Una cita genera a lo sumo un registro. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cita_id", unique = true)
    private Cita cita;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Empleado veterinario;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    // ---------- Anamnesis ----------

    @Column(nullable = false, length = 255)
    private String motivo;

    /** Lo que relata el propietario: desde cuando, como empezo, que cambio. */
    @Column(length = 1000)
    private String anamnesis;

    // ---------- Constantes vitales ----------
    // Se registran por separado y no dentro del texto del examen porque
    // su valor esta en poder compararlas entre consultas: un peso o una
    // temperatura solo significan algo frente a los de la vez anterior.

    @Column(name = "temperatura_c")
    private Double temperaturaC;

    @Column(name = "frecuencia_cardiaca")
    private Integer frecuenciaCardiaca;

    @Column(name = "frecuencia_respiratoria")
    private Integer frecuenciaRespiratoria;

    /** Peso tomado en la consulta: permite seguir la evolucion del paciente. */
    @Column(name = "peso_kg")
    private Double pesoKg;

    /** Condicion corporal en escala de 1 a 5 (1 caquexia, 5 obesidad). */
    @Column(name = "condicion_corporal")
    private Integer condicionCorporal;

    // ---------- Examen, diagnostico y plan ----------

    @Column(name = "examen_fisico", length = 1000)
    private String examenFisico;

    @Column(nullable = false, length = 500)
    private String diagnostico;

    @Column(length = 500)
    private String tratamiento;

    @Column(name = "medicamentos", length = 500)
    private String medicamentos;

    @Column(name = "vacunas_aplicadas", length = 255)
    private String vacunasAplicadas;

    @Column(length = 255)
    private String desparasitacion;

    @Column(name = "proximo_control")
    private LocalDate proximoControl;

    @Column(length = 500)
    private String observaciones;

    /**
     * Fabrica de dominio: construye el registro validando sus propios
     * invariantes. Al no haber constructor publico con todos los campos
     * sueltos, es imposible crear un historial incompleto.
     */
    public static HistorialClinico registrar(Mascota mascota, Cita cita, Empleado veterinario,
                                             String motivo, String diagnostico) {
        // RN-07: el historial clinico solo lo crea un veterinario.
        //
        // La comprobacion se hace por el rol que el objeto declara y NO con
        // "instanceof Veterinario". La razon es concreta: cuando el empleado
        // llega desde Usuario.getEmpleado(), que es una relacion perezosa,
        // Hibernate entrega un proxy que hereda de Empleado pero no de
        // Veterinario. El instanceof daba falso para una veterinaria real y
        // rechazaba su propio registro. El proxy, en cambio, delega las
        // llamadas al objeto verdadero, de modo que getRol() si responde
        // correctamente. El polimorfismo se conserva: sigue siendo el
        // subtipo quien declara su rol.
        if (!"VETERINARIO".equals(veterinario.getRol())) {
            throw new ReglaNegocioException(
                "RN-07: solo un veterinario puede registrar el historial clinico. "
                + "La cuenta corresponde a un perfil " + veterinario.getRol() + ".");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new ReglaNegocioException("El motivo de la consulta es obligatorio.");
        }
        if (diagnostico == null || diagnostico.isBlank()) {
            throw new ReglaNegocioException("El diagnostico es obligatorio.");
        }
        HistorialClinico h = new HistorialClinico();
        h.mascota = mascota;
        h.cita = cita;
        h.veterinario = veterinario;
        h.motivo = motivo.trim();
        h.diagnostico = diagnostico.trim();
        h.fecha = cita != null ? cita.getFechaHora() : LocalDateTime.now();
        return h;
    }

    /** Constructor sin argumentos requerido por JPA. */
    protected HistorialClinico() { }

    public Long getId() { return id; }
    public Mascota getMascota() { return mascota; }
    public Cita getCita() { return cita; }
    public Empleado getVeterinario() { return veterinario; }
    public LocalDateTime getFecha() { return fecha; }
    public String getMotivo() { return motivo; }
    public String getDiagnostico() { return diagnostico; }

    public String getAnamnesis() { return anamnesis; }
    public void setAnamnesis(String anamnesis) { this.anamnesis = anamnesis; }
    public Double getTemperaturaC() { return temperaturaC; }
    public void setTemperaturaC(Double temperaturaC) { this.temperaturaC = temperaturaC; }
    public Integer getFrecuenciaCardiaca() { return frecuenciaCardiaca; }
    public void setFrecuenciaCardiaca(Integer frecuenciaCardiaca) { this.frecuenciaCardiaca = frecuenciaCardiaca; }
    public Integer getFrecuenciaRespiratoria() { return frecuenciaRespiratoria; }
    public void setFrecuenciaRespiratoria(Integer frecuenciaRespiratoria) { this.frecuenciaRespiratoria = frecuenciaRespiratoria; }
    public Integer getCondicionCorporal() { return condicionCorporal; }
    public void setCondicionCorporal(Integer condicionCorporal) { this.condicionCorporal = condicionCorporal; }
    public String getExamenFisico() { return examenFisico; }
    public void setExamenFisico(String examenFisico) { this.examenFisico = examenFisico; }
    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }
    public String getMedicamentos() { return medicamentos; }
    public void setMedicamentos(String medicamentos) { this.medicamentos = medicamentos; }
    public String getVacunasAplicadas() { return vacunasAplicadas; }
    public void setVacunasAplicadas(String vacunasAplicadas) { this.vacunasAplicadas = vacunasAplicadas; }
    public String getDesparasitacion() { return desparasitacion; }
    public void setDesparasitacion(String desparasitacion) { this.desparasitacion = desparasitacion; }
    public LocalDate getProximoControl() { return proximoControl; }
    public void setProximoControl(LocalDate proximoControl) { this.proximoControl = proximoControl; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
    public Double getPesoKg() { return pesoKg; }
    public void setPesoKg(Double pesoKg) { this.pesoKg = pesoKg; }
}
