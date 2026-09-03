package com.hogarmascotas.api.service;

import com.hogarmascotas.api.entity.*;
import com.hogarmascotas.api.exception.ReglaNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias PURAS de POO (sin Spring ni base de datos):
 * demuestran que el polimorfismo y el encapsulamiento funcionan
 * en los propios objetos del dominio. Evidencia directa para la
 * sustentacion de los pilares POO.
 */
class InventarioPolimorfismoTest {

    private Producto producto;

    @BeforeEach
    void preparar() {
        producto = new Producto();
        producto.setNombre("Antipulgas pipeta");
        producto.setCategoria("MEDICAMENTO");
        producto.setPrecio(new BigDecimal("38000"));
        producto.setStockMinimo(10);
        producto.ingresarStock(30); // stock inicial: 30
    }

    @Test
    @DisplayName("POLIMORFISMO: la misma llamada aplicar() suma en Entrada y resta en Salida")
    void aplicarEsPolimorfico() {
        MovimientoInventario entrada = new EntradaInventario();
        entrada.setCantidad(10);

        MovimientoInventario salida = new SalidaInventario();
        salida.setCantidad(5);

        // El codigo cliente no pregunta el tipo: solo invoca aplicar()
        entrada.aplicar(producto);
        assertEquals(40, producto.getStockActual());

        salida.aplicar(producto);
        assertEquals(35, producto.getStockActual());
    }

    @Test
    @DisplayName("RN-05: una salida mayor al stock disponible es rechazada por el propio objeto")
    void salidaMayorAlStockEsRechazada() {
        MovimientoInventario salida = new SalidaInventario();
        salida.setCantidad(999);

        ReglaNegocioException error = assertThrows(ReglaNegocioException.class,
                () -> salida.aplicar(producto));

        assertTrue(error.getMessage().contains("RN-05"));
        assertEquals(30, producto.getStockActual()); // el stock no cambio
    }

    @Test
    @DisplayName("RF-13: requiereReposicion() detecta el stock en o bajo el minimo")
    void alertaDeStockMinimo() {
        assertFalse(producto.requiereReposicion()); // 30 > 10

        MovimientoInventario salida = new SalidaInventario();
        salida.setCantidad(20);
        salida.aplicar(producto); // queda en 10 = minimo

        assertTrue(producto.requiereReposicion());
    }

    @Test
    @DisplayName("POLIMORFISMO: calcularPrecio() varia por subtipo de servicio y tamano")
    void precioPolimorfico() {
        Servicio consulta = new ConsultaVeterinaria();
        consulta.setPrecioBase(new BigDecimal("60000"));

        ServicioSpa bano = new ServicioSpa();
        bano.setPrecioBase(new BigDecimal("40000"));
        bano.setRecargoPorTamano(new BigDecimal("10000"));

        // La consulta no varia por tamano
        assertEquals(new BigDecimal("60000"), consulta.calcularPrecio(Mascota.Tamano.GRANDE));
        // El spa si: base, base + recargo, base + 2*recargo
        assertEquals(new BigDecimal("40000"), bano.calcularPrecio(Mascota.Tamano.PEQUENO));
        assertEquals(new BigDecimal("50000"), bano.calcularPrecio(Mascota.Tamano.MEDIANO));
        assertEquals(new BigDecimal("60000"), bano.calcularPrecio(Mascota.Tamano.GRANDE));
    }

    @Test
    @DisplayName("RN-04 con POLIMORFISMO: cada servicio sabe quien puede atenderlo")
    void compatibilidadRolServicio() {
        Servicio consulta = new ConsultaVeterinaria();
        Servicio spa = new ServicioSpa();
        Empleado veterinario = new Veterinario();
        Empleado esteticista = new Esteticista();

        assertTrue(consulta.puedeSerAtendidoPor(veterinario));
        assertFalse(consulta.puedeSerAtendidoPor(esteticista));
        assertTrue(spa.puedeSerAtendidoPor(esteticista));
        assertFalse(spa.puedeSerAtendidoPor(veterinario));
    }
}
