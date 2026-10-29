package com.hogarmascotas.api.service;

import com.hogarmascotas.api.entity.*;
import com.hogarmascotas.api.exception.ReglaNegocioException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Historia clinica (RF-09, RF-10) y la regla RN-07.
 *
 * Verifica dos cosas distintas:
 *  - que el POLIMORFISMO decide si una atencion genera historia, sin
 *    que nadie pregunte por el tipo de servicio;
 *  - que la ENTIDAD protege sus propios invariantes, de modo que es
 *    imposible construir un registro clinico invalido.
 */
class HistorialClinicoTest {

    private Mascota mascota() {
        Mascota m = new Mascota();
        m.setNombre("Rocky");
        m.setEspecie("PERRO");
        m.setTamano(Mascota.Tamano.GRANDE);
        return m;
    }

    private Veterinario veterinaria() {
        Veterinario v = new Veterinario();
        v.setNombres("Arlin");
        v.setApellidos("Gaviria");
        return v;
    }

    private Esteticista esteticista() {
        Esteticista e = new Esteticista();
        e.setNombres("Candelaria");
        e.setApellidos("Silgado");
        return e;
    }

    // ---------- Polimorfismo ----------

    @Test
    @DisplayName("POLIMORFISMO: la consulta genera historia clinica; el spa no")
    void soloLaConsultaGeneraHistoria() {
        Servicio consulta = new ConsultaVeterinaria();
        Servicio bano = new ServicioSpa();

        // El codigo que llama no sabe de que subtipo se trata: solo
        // pregunta. Agregar un tipo nuevo no obliga a tocar esta logica.
        assertTrue(consulta.requiereHistorialClinico());
        assertFalse(bano.requiereHistorialClinico());
    }

    @Test
    @DisplayName("El recargo por tamano solo aplica a los servicios de spa")
    void precioSegunSubtipo() {
        ConsultaVeterinaria consulta = new ConsultaVeterinaria();
        consulta.setPrecioBase(new BigDecimal("60000"));

        ServicioSpa bano = new ServicioSpa();
        bano.setPrecioBase(new BigDecimal("55000"));
        bano.setRecargoPorTamano(new BigDecimal("15000"));

        // Una consulta cuesta lo mismo para cualquier mascota...
        assertEquals(0, consulta.calcularPrecio(Mascota.Tamano.GRANDE)
                .compareTo(new BigDecimal("60000")));
        // ...y un bano de mascota grande suma dos recargos.
        assertEquals(0, bano.calcularPrecio(Mascota.Tamano.GRANDE)
                .compareTo(new BigDecimal("85000")));
    }

    // ---------- Invariantes de la entidad ----------

    @Test
    @DisplayName("RN-07: una esteticista no puede crear historia clinica")
    void soloVeterinarioRegistra() {
        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> HistorialClinico.registrar(mascota(), null, esteticista(),
                        "Bano mensual", "No aplica"));
        assertTrue(error.getMessage().contains("RN-07"));
    }

    @Test
    @DisplayName("El diagnostico es obligatorio: no existe historia sin el")
    void diagnosticoObligatorio() {
        assertThrows(ReglaNegocioException.class,
                () -> HistorialClinico.registrar(mascota(), null, veterinaria(),
                        "Chequeo anual", "   "));
    }

    @Test
    @DisplayName("El motivo es obligatorio")
    void motivoObligatorio() {
        assertThrows(ReglaNegocioException.class,
                () -> HistorialClinico.registrar(mascota(), null, veterinaria(),
                        null, "Paciente sano"));
    }

    @Test
    @DisplayName("Un registro valido queda completo y asociado a su paciente")
    void registroValido() {
        Mascota rocky = mascota();
        HistorialClinico h = HistorialClinico.registrar(rocky, null, veterinaria(),
                "  Chequeo anual  ", "Paciente sano");

        assertEquals("Chequeo anual", h.getMotivo()); // se normaliza
        assertEquals("Paciente sano", h.getDiagnostico());
        assertSame(rocky, h.getMascota());
        assertNotNull(h.getFecha());
    }

    @Test
    @DisplayName("DEF-09: un proxy perezoso de una veterinaria si puede registrar")
    void proxyDeVeterinariaEsAceptado() {
        // Regresion del defecto DEF-09. Cuando el empleado llega desde
        // Usuario.getEmpleado(), que es una relacion perezosa, Hibernate
        // entrega un proxy que hereda de Empleado pero NO de Veterinario:
        // la validacion hecha con instanceof rechazaba a una veterinaria
        // autentica. Aqui se simula ese objeto con una subclase directa de
        // Empleado que declara el rol correcto, igual que hace el proxy.
        Empleado comoProxy = new Empleado() {
            @Override public String getRol() { return "VETERINARIO"; }
        };
        comoProxy.setNombres("Arlin");
        comoProxy.setApellidos("Gaviria");

        HistorialClinico h = HistorialClinico.registrar(mascota(), null, comoProxy,
                "Chequeo anual", "Paciente sano");

        assertNotNull(h);
        assertEquals("Arlin Gaviria", h.getVeterinario().getNombreCompleto());
    }

    @Test
    @DisplayName("RN-04 resiste el mismo caso: el rol decide, no la clase")
    void compatibilidadPorRolYNoPorClase() {
        Empleado vetComoProxy = new Empleado() {
            @Override public String getRol() { return "VETERINARIO"; }
        };
        Empleado estComoProxy = new Empleado() {
            @Override public String getRol() { return "ESTETICISTA"; }
        };

        Servicio consulta = new ConsultaVeterinaria();
        Servicio bano = new ServicioSpa();

        assertTrue(consulta.puedeSerAtendidoPor(vetComoProxy));
        assertFalse(consulta.puedeSerAtendidoPor(estComoProxy));
        assertTrue(bano.puedeSerAtendidoPor(estComoProxy));
        assertFalse(bano.puedeSerAtendidoPor(vetComoProxy));
    }

    @Test
    @DisplayName("ENCAPSULAMIENTO: el diagnostico no tiene setter publico")
    void diagnosticoInmutable() {
        // Una historia clinica no se reescribe: se corrige agregando un
        // registro nuevo. Que no exista setDiagnostico() lo garantiza.
        boolean existeSetter = java.util.Arrays
                .stream(HistorialClinico.class.getMethods())
                .anyMatch(m -> m.getName().equals("setDiagnostico")
                            || m.getName().equals("setMotivo"));
        assertFalse(existeSetter,
                "La historia clinica no debe permitir reescribir motivo ni diagnostico");
    }
}
