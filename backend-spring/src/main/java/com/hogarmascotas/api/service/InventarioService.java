package com.hogarmascotas.api.service;

import com.hogarmascotas.api.dto.InventarioDtos.MovimientoRequest;
import com.hogarmascotas.api.dto.InventarioDtos.MovimientoResponse;
import com.hogarmascotas.api.dto.InventarioDtos.ProductoRequest;
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

    /** Vista de administracion: incluye los productos descontinuados. */
    public List<ProductoResponse> listarTodos() {
        return productoRepo.findAll().stream()
                .map(ProductoResponse::desde).toList();
    }

    /** RF-13: lo que hay que reponer, para armar el pedido al proveedor. */
    public List<ProductoResponse> porReponer() {
        return productoRepo.findByActivoTrue().stream()
                .filter(Producto::requiereReposicion)
                .map(ProductoResponse::desde).toList();
    }

    /**
     * RF-11: alta de producto. Nace con stock en cero a proposito:
     * la existencia solo entra por un movimiento registrado, de modo
     * que toda unidad en bodega tiene su respaldo documental.
     */
    @Transactional
    public ProductoResponse crear(ProductoRequest datos) {
        productoRepo.findByActivoTrue().stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(datos.nombre().trim()))
                .findAny()
                .ifPresent(p -> { throw new ReglaNegocioException(
                        "Ya existe un producto activo llamado '" + p.getNombre() + "'."); });

        Producto producto = new Producto();
        aplicar(producto, datos);
        return ProductoResponse.desde(productoRepo.save(producto));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest datos) {
        Producto producto = productoRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));
        aplicar(producto, datos);
        return ProductoResponse.desde(producto);
    }

    /**
     * RF-11: baja LOGICA. Un producto con movimientos historicos no se
     * elimina: dejaria movimientos huerfanos y rompería la trazabilidad.
     */
    @Transactional
    public ProductoResponse cambiarEstado(Long id, boolean activo) {
        Producto producto = productoRepo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado."));
        if (!activo && producto.getStockActual() > 0) {
            throw new ReglaNegocioException(
                "El producto aun tiene " + producto.getStockActual()
                + " unidades en existencia. Registre la salida antes de descontinuarlo.");
        }
        producto.setActivo(activo);
        return ProductoResponse.desde(producto);
    }

    /** RNF-10: historial de movimientos de un producto. */
    public List<MovimientoResponse> movimientosDe(Long productoId) {
        if (!productoRepo.existsById(productoId)) {
            throw new RecursoNoEncontradoException("Producto no encontrado.");
        }
        return movimientoRepo.findByProductoIdOrderByFechaDesc(productoId)
                .stream().map(MovimientoResponse::desde).toList();
    }

    private void aplicar(Producto producto, ProductoRequest datos) {
        producto.setNombre(datos.nombre().trim());
        producto.setCategoria(datos.categoria().trim().toUpperCase());
        producto.setDescripcion(datos.descripcion());
        producto.setPrecio(datos.precio());
        producto.setStockMinimo(datos.stockMinimo());
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
