package com.hogarmascotas.api.dto;

import com.hogarmascotas.api.entity.Producto;
import com.hogarmascotas.api.entity.SalidaInventario;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class InventarioDtos {

    public record ProductoResponse(
            Long id, String nombre, String categoria, String descripcion,
            BigDecimal precio, int stockActual, int stockMinimo,
            boolean requiereReposicion
    ) {
        public static ProductoResponse desde(Producto p) {
            return new ProductoResponse(p.getId(), p.getNombre(), p.getCategoria(),
                    p.getDescripcion(), p.getPrecio(), p.getStockActual(),
                    p.getStockMinimo(), p.requiereReposicion());
        }
    }

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
}
