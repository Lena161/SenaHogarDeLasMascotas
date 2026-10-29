// ============================================================
// Inventario (RF-11, RF-12, RF-13).
//
// El stock no es un campo editable: se mueve registrando entradas
// y salidas. Esa restricción viene del modelo —la entidad Producto
// no expone setStockActual()— y la interfaz la respeta en vez de
// disimularla, porque es lo que garantiza que toda existencia
// tenga su respaldo documental.
// ============================================================
import { useEffect, useState } from 'react';
import { api } from '../../services/api';
import { useAuth } from '../../context/AuthContext';

const PRODUCTO_VACIO = {
  nombre: '', categoria: 'ALIMENTO', descripcion: '', precio: '', stockMinimo: 5
};

const CATEGORIAS = ['ALIMENTO', 'MEDICAMENTO', 'HIGIENE', 'ACCESORIO', 'INSUMO'];

function pesos(valor) {
  return new Intl.NumberFormat('es-CO',
    { style: 'currency', currency: 'COP', maximumFractionDigits: 0 }).format(valor);
}

export default function Inventario() {
  const { usuario } = useAuth();
  const esAdmin = usuario.rol === 'ADMIN';

  const [productos, setProductos] = useState(null);
  const [mensaje, setMensaje] = useState(null);
  const [movimiento, setMovimiento] = useState(null);
  const [editando, setEditando] = useState(null);
  const [movimientos, setMovimientos] = useState(null);

  useEffect(() => { cargar(); }, []);

  async function cargar() {
    try {
      setProductos(await api.productosTodos());
    } catch (e) {
      setProductos([]);
      setMensaje({ tipo: 'error', texto: e.message });
    }
  }

  async function accion(fn, exito) {
    setMensaje(null);
    try {
      await fn();
      setMensaje({ tipo: 'ok', texto: exito });
      await cargar();
    } catch (e) {
      setMensaje({ tipo: 'error', texto: e.message });
    }
  }

  async function verMovimientos(p) {
    setMensaje(null);
    try {
      setMovimientos({ producto: p, lista: await api.movimientosDeProducto(p.id) });
    } catch (e) {
      setMensaje({ tipo: 'error', texto: e.message });
    }
  }

  const porReponer = (productos || []).filter((p) => p.activo && p.requiereReposicion);

  return (
    <section>
      {mensaje && (
        <p className={mensaje.tipo === 'error' ? 'aviso-error' : 'aviso-ok'}>
          {mensaje.texto}
        </p>
      )}

      {porReponer.length > 0 && (
        <div className="alerta-reposicion">
          <strong>{porReponer.length} producto(s) en o por debajo del stock mínimo:</strong>{' '}
          {porReponer.map((p) => p.nombre).join(', ')}
        </div>
      )}

      <div className="panel-barra">
        {esAdmin && (
          <button type="button" className="btn-solido"
                  onClick={() => setEditando({ ...PRODUCTO_VACIO, id: null })}>
            + Nuevo producto
          </button>
        )}
        <span className="panel-conteo">
          {productos === null ? 'Cargando…' : `${productos.length} producto(s)`}
        </span>
      </div>

      {productos !== null && (
        <div className="tabla-envoltura">
          <table className="tabla-panel">
            <thead>
              <tr>
                <th>Producto</th>
                <th>Categoría</th>
                <th>Precio</th>
                <th>Stock</th>
                <th>Acciones</th>
              </tr>
            </thead>
            <tbody>
              {productos.map((p) => (
                <tr key={p.id} className={p.activo ? '' : 'fila-inactiva'}>
                  <td>
                    <strong>{p.nombre}</strong>
                    {p.descripcion && <small>{p.descripcion}</small>}
                    {!p.activo && <small>Descontinuado</small>}
                  </td>
                  <td>{p.categoria}</td>
                  <td>{pesos(p.precio)}</td>
                  <td>
                    <span className={p.requiereReposicion ? 'stock bajo' : 'stock'}>
                      {p.stockActual}
                    </span>
                    <small>mínimo {p.stockMinimo}</small>
                  </td>
                  <td className="celda-acciones">
                    {p.activo && (
                      <button type="button" className="btn-mini principal"
                              onClick={() => setMovimiento({ producto: p, tipo: 'ENTRADA' })}>
                        Movimiento
                      </button>
                    )}
                    <button type="button" className="btn-mini"
                            onClick={() => verMovimientos(p)}>
                      Historial
                    </button>
                    {esAdmin && p.activo && (
                      <>
                        <button type="button" className="btn-mini"
                                onClick={() => setEditando({ ...p })}>
                          Editar
                        </button>
                        <button type="button" className="btn-mini peligro"
                                onClick={() => accion(() => api.descontinuarProducto(p.id),
                                                      'Producto descontinuado.')}>
                          Descontinuar
                        </button>
                      </>
                    )}
                    {esAdmin && !p.activo && (
                      <button type="button" className="btn-mini"
                              onClick={() => accion(() => api.reactivarProducto(p.id),
                                                    'Producto reactivado.')}>
                        Reactivar
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {movimiento && (
        <FormMovimiento datos={movimiento}
                        onCerrar={() => setMovimiento(null)}
                        onGuardar={async (cuerpo) => {
                          await api.registrarMovimiento(cuerpo);
                          setMovimiento(null);
                          setMensaje({ tipo: 'ok', texto: 'Movimiento registrado.' });
                          await cargar();
                        }} />
      )}

      {editando && (
        <FormProducto producto={editando}
                      onCerrar={() => setEditando(null)}
                      onGuardar={async (cuerpo) => {
                        if (editando.id) await api.actualizarProducto(editando.id, cuerpo);
                        else await api.crearProducto(cuerpo);
                        setEditando(null);
                        setMensaje({ tipo: 'ok', texto: 'Producto guardado.' });
                        await cargar();
                      }} />
      )}

      {movimientos && (
        <div className="modal-fondo" role="dialog" aria-modal="true">
          <div className="modal ancho">
            <header className="modal-cabecera">
              <h2>Movimientos de {movimientos.producto.nombre}</h2>
              <p className="modal-sub">
                Trazabilidad completa: cada unidad tiene su registro.
              </p>
            </header>
            <div className="modal-cuerpo">
              {movimientos.lista.length === 0 && (
                <p className="panel-vacio">Sin movimientos registrados.</p>
              )}
              {movimientos.lista.length > 0 && (
                <table className="tabla-panel compacta">
                  <thead>
                    <tr>
                      <th>Fecha</th><th>Tipo</th><th>Cantidad</th>
                      <th>Motivo</th><th>Responsable</th>
                    </tr>
                  </thead>
                  <tbody>
                    {movimientos.lista.map((m) => (
                      <tr key={m.id}>
                        <td>{new Date(m.fecha).toLocaleDateString('es-CO')}</td>
                        <td>
                          <span className={m.tipo === 'ENTRADA'
                            ? 'estado-badge estado-confirmada'
                            : 'estado-badge estado-pendiente'}>
                            {m.tipo}
                          </span>
                        </td>
                        <td>{m.cantidad}</td>
                        <td>
                          {m.motivo}
                          {m.destino && <small>{m.destino.replace('_', ' ')}</small>}
                          {m.proveedor && <small>{m.proveedor}</small>}
                        </td>
                        <td>{m.responsable}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
              <div className="modal-acciones">
                <button type="button" className="btn-plano"
                        onClick={() => setMovimientos(null)}>Cerrar</button>
              </div>
            </div>
          </div>
        </div>
      )}
    </section>
  );
}

/** Registro de entrada o salida (RF-12). */
function FormMovimiento({ datos, onGuardar, onCerrar }) {
  const [tipo, setTipo] = useState('ENTRADA');
  const [cantidad, setCantidad] = useState(1);
  const [motivo, setMotivo] = useState('');
  const [proveedor, setProveedor] = useState('');
  const [destino, setDestino] = useState('VENTA');
  const [error, setError] = useState(null);

  async function enviar(e) {
    e.preventDefault();
    setError(null);
    try {
      await onGuardar({
        productoId: datos.producto.id,
        tipo, cantidad: Number(cantidad), motivo,
        proveedor: tipo === 'ENTRADA' ? proveedor : null,
        destino: tipo === 'SALIDA' ? destino : null
      });
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="modal-fondo" role="dialog" aria-modal="true">
      <div className="modal">
        <header className="modal-cabecera">
          <h2>Registrar movimiento</h2>
          <p className="modal-sub">
            {datos.producto.nombre} · existencia actual: {datos.producto.stockActual}
          </p>
        </header>
        <form className="modal-cuerpo" onSubmit={enviar}>
          <div className="fila-doble">
            <label>Tipo
              <select value={tipo} onChange={(e) => setTipo(e.target.value)}>
                <option value="ENTRADA">Entrada (compra)</option>
                <option value="SALIDA">Salida</option>
              </select>
            </label>
            <label>Cantidad
              <input type="number" min="1" required value={cantidad}
                     onChange={(e) => setCantidad(e.target.value)} />
            </label>
          </div>

          <label>Motivo
            <input required maxLength={150} value={motivo}
                   onChange={(e) => setMotivo(e.target.value)}
                   placeholder={tipo === 'ENTRADA'
                     ? 'Compra a proveedor' : 'Venta a cliente'} />
          </label>

          {tipo === 'ENTRADA' && (
            <label>Proveedor
              <input value={proveedor} onChange={(e) => setProveedor(e.target.value)} />
            </label>
          )}

          {tipo === 'SALIDA' && (
            <label>Destino
              <select value={destino} onChange={(e) => setDestino(e.target.value)}>
                <option value="VENTA">Venta</option>
                <option value="CONSUMO_INTERNO">Consumo interno</option>
                <option value="BAJA">Baja</option>
              </select>
            </label>
          )}

          {tipo === 'SALIDA' && (
            <p className="nota">
              El sistema rechaza la salida si supera la existencia. Si el conteo
              físico no coincide, hay un movimiento que no se registró.
            </p>
          )}

          {error && <p className="aviso-error">{error}</p>}

          <div className="modal-acciones">
            <button type="button" className="btn-plano" onClick={onCerrar}>Cancelar</button>
            <button type="submit" className="btn-solido">Registrar</button>
          </div>
        </form>
      </div>
    </div>
  );
}

/** Alta y edición de productos (RF-11). */
function FormProducto({ producto, onGuardar, onCerrar }) {
  const [datos, setDatos] = useState({
    nombre: producto.nombre || '',
    categoria: producto.categoria || 'ALIMENTO',
    descripcion: producto.descripcion || '',
    precio: producto.precio || '',
    stockMinimo: producto.stockMinimo ?? 5
  });
  const [error, setError] = useState(null);

  function cambiar(e) {
    setDatos({ ...datos, [e.target.name]: e.target.value });
  }

  async function enviar(e) {
    e.preventDefault();
    setError(null);
    try {
      await onGuardar({
        ...datos,
        precio: Number(datos.precio),
        stockMinimo: Number(datos.stockMinimo)
      });
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <div className="modal-fondo" role="dialog" aria-modal="true">
      <div className="modal">
        <header className="modal-cabecera">
          <h2>{producto.id ? 'Editar producto' : 'Nuevo producto'}</h2>
        </header>
        <form className="modal-cuerpo" onSubmit={enviar}>
          <label>Nombre
            <input name="nombre" required maxLength={100}
                   value={datos.nombre} onChange={cambiar} />
          </label>

          <div className="fila-doble">
            <label>Categoría
              <select name="categoria" value={datos.categoria} onChange={cambiar}>
                {CATEGORIAS.map((c) => <option key={c} value={c}>{c}</option>)}
              </select>
            </label>
            <label>Precio
              <input name="precio" type="number" min="1" required
                     value={datos.precio} onChange={cambiar} />
            </label>
          </div>

          <label>Descripción
            <input name="descripcion" maxLength={255}
                   value={datos.descripcion} onChange={cambiar} />
          </label>

          <label>Stock mínimo
            <input name="stockMinimo" type="number" min="0" required
                   value={datos.stockMinimo} onChange={cambiar} />
          </label>

          {!producto.id && (
            <p className="nota">
              El producto nace con existencia en cero. Cárguelo con un
              movimiento de entrada: así queda registrado de dónde vino.
            </p>
          )}

          {error && <p className="aviso-error">{error}</p>}

          <div className="modal-acciones">
            <button type="button" className="btn-plano" onClick={onCerrar}>Cancelar</button>
            <button type="submit" className="btn-solido">Guardar</button>
          </div>
        </form>
      </div>
    </div>
  );
}
