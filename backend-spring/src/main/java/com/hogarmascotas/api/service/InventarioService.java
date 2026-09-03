package com.hogarmascotas.api.service;

import com.hogarmascotas.api.dto.InventarioDtos.MovimientoRequest;
import com.hogarmascotas.api.dto.InventarioDtos.ProductoResponse;
import com.hogarmascotas.api.entity.*;
import com.hogarmascotas.api.exception.RecursoNoEncontradoException;
import com.hogarmascotas.api.exception.ReglaNegocioException;
import com.hogarmascotas.api.repository.MovimientoInventarioRepository;
import com.hogarmascotas.api.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Inventario (RF-11, RF-12, RF-13). Defensa en profundidad:
 * la entidad Producto protege RN-05/RNF-08 en el codigo, y los
 * triggers de la Fase 2 lo garantizan en la base de datos.
 */
@Service
public class InventarioService {

    private final ProductoRepository productoRepo;
    private final MovimientoInventarioRepository movimientoRepo;

    public InventarioService(ProductoRepository productoRepo,
                             MovimientoInventarioRepository movimientoRepo) {
        this.productoRepo = productoRepo;
        this.movimientoRepo = movimientoRepo;
    }

    public List<ProductoResponse> listarActivos() {
        return productoRepo.findByActivoTrue().stream()
                .map(ProductoResponse::desde).toList();
    }

    @Transactional
    public ProductoResponse registrarMovimiento(MovimientoRequest datos, Usuario responsable) {
        Producto producto = productoRepo.findById(datos.productoId())
                .filter(Producto::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));

        // Se construye el subtipo adecuado, validando los datos que
        // exige cada tipo (coherencia con el CHECK chk_mov_salida de la
        // Fase 2: mejor un 409 claro aqui que un error crudo de la BD).
        MovimientoInventario movimiento = switch (datos.tipo()) {
            case ENTRADA -> {
                EntradaInventario e = new EntradaInventario();
                e.setProveedor(datos.proveedor());
                yield e;
            }
            case SALIDA -> {
                if (datos.destino() == null) {
                    throw new ReglaNegocioException(
                        "Toda salida debe indicar su destino (VENTA, CONSUMO_INTERNO o BAJA).");
                }
                SalidaInventario s = new SalidaInventario();
                s.setDestino(datos.destino());
                yield s;
            }
        };
        movimiento.setProducto(producto);
        movimiento.setUsuario(responsable); // RNF-10: trazabilidad
        movimiento.setCantidad(datos.cantidad());
        movimiento.setMotivo(datos.motivo());

        // ...y desde aqui el codigo es POLIMORFICO: no hay if por tipo.
        // La entrada suma; la salida valida RN-05 y resta.
        movimiento.aplicar(producto);

        movimientoRepo.save(movimiento);
        return ProductoResponse.desde(producto);
    }
}
