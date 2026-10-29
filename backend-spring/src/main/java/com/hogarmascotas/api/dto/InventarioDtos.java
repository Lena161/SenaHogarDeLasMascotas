package com.hogarmascotas.api.dto;

import com.hogarmascotas.api.entity.EntradaInventario;
import com.hogarmascotas.api.entity.MovimientoInventario;
import com.hogarmascotas.api.entity.Producto;
import com.hogarmascotas.api.entity.SalidaInventario;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class InventarioDtos {

    public record ProductoResponse(
            Long id, String nombre, String categoria, String descripcion,
            BigDecimal precio, int stockActual, int stockMinimo,
            boolean requiereReposicion, boolean activo
    ) {
        public static ProductoResponse desde(Producto p) {
            return new ProductoResponse(p.getId(), p.getNombre(), p.getCategoria(),
                    p.getDescripcion(), p.getPrecio(), p.getStockActual(),
                    p.getStockMinimo(), p.requiereReposicion(), p.isActivo());
        }
    }

    /**
     * Alta y edicion de productos (RF-11).
     *
     * Observe que NO incluye el stock. El stock solo cambia por
     * movimientos registrados: un producto nuevo nace en cero y se
     * carga con una entrada. Asi ninguna existencia queda sin
     * explicacion, que es justamente lo que protege la entidad al no
     * exponer setStockActual().
     */
    public record ProductoRequest(
            @NotBlank @Size(max = 100) String nombre,
            @NotBlank @Size(max = 20) String categoria,
            @Size(max = 255) String descripcion,
            @NotNull @DecimalMin(value = "0.0", inclusive = false,
                    message = "El precio debe ser mayor que cero") BigDecimal precio,
            @Min(value = 0, message = "El stock minimo no puede ser negativo") int stockMinimo
    ) {}

    public record MovimientoRequest(
            @NotNull Long productoId,
            @NotNull TipoMovimiento tipo,
            @Positive int cantidad,
            @NotBlank String motivo,
            String proveedor,                    // solo ENTRADA
            SalidaInventario.Destino destino    // solo SALIDA
    ) {
        public enum TipoMovimiento { ENTRADA, SALIDA }
    }

    /** RNF-10: la trazabilidad de cada movimiento del producto. */
    public record MovimientoResponse(
            Long id, String tipo, int cantidad, String motivo,
            String proveedor, String destino, String responsable, LocalDateTime fecha
    ) {
        public static MovimientoResponse desde(MovimientoInventario m) {
            String destino = (m instanceof SalidaInventario s && s.getDestino() != null)
                    ? s.getDestino().name() : null;
            String proveedor = (m instanceof EntradaInventario e) ? e.getProveedor() : null;
            String responsable = m.getUsuario() == null ? "—"
                    : (m.getUsuario().getEmpleado() != null
                        ? m.getUsuario().getEmpleado().getNombreCompleto()
                        : m.getUsuario().getUsername());
            return new MovimientoResponse(m.getId(), m.getTipo(), m.getCantidad(),
                    m.getMotivo(), proveedor, destino, responsable, m.getFecha());
        }
    }
}
