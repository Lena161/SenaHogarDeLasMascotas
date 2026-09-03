// Productos — CONMUTADO: el catálogo por fin sale del inventario
// real de MySQL (RF-11), cerrando la deuda documentada del arreglo
// quemado. El archivo src/data/productos.jsx queda obsoleto.
import { useEffect, useState } from 'react';
import Hero from '../components/Hero';
import ProductCard from '../components/ProductCard';
import {
  IconoConsulta, IconoBano, IconoLaboratorio, IconoTienda
} from '../components/Iconos';
import { api } from '../services/api';

// Icono según la categoría del inventario (mismas del modelo Fase 2)
const ICONOS_CATEGORIA = {
  ALIMENTO: <IconoConsulta />,
  MEDICAMENTO: <IconoLaboratorio />,
  HIGIENE: <IconoBano />,
  ACCESORIO: <IconoTienda />,
  INSUMO: <IconoTienda />
};

function formatearPrecio(valor) {
  return new Intl.NumberFormat('es-CO', {
    style: 'currency', currency: 'COP', maximumFractionDigits: 0
  }).format(valor);
}

export default function Productos() {
  const [productos, setProductos] = useState(null);

  useEffect(() => {
    api.productos()
      .then(setProductos)
      .catch(() => setProductos([]));
  }, []);

  return (
    <main>
      <Hero titulo="Nuestros Productos"
            subtitulo="Todo lo que tu mascota necesita, en un solo lugar. Escríbenos por WhatsApp para comprar o consultar disponibilidad." />

      <div className="productos-grid">
        {productos === null && <p className="citas-empty">Cargando catálogo...</p>}
        {productos !== null && productos.length === 0 && (
          <p className="citas-empty">
            No pudimos cargar el catálogo. Verifica que el backend esté corriendo.
          </p>
        )}
        {productos !== null && productos.map((p) => (
          <ProductCard key={p.id}
                       categoria={p.categoria.charAt(0) + p.categoria.slice(1).toLowerCase()}
                       nombre={p.nombre}
                       desc={p.descripcion}
                       precio={formatearPrecio(p.precio)}
                       icono={ICONOS_CATEGORIA[p.categoria] || <IconoTienda />} />
        ))}
      </div>
    </main>
  );
}