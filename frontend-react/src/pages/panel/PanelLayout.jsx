// ============================================================
// Estructura común del panel del personal.
//
// La navegación se arma según el rol: cada perfil ve solo lo que
// puede hacer. Esto evita que alguien llegue a una pantalla que
// el backend le va a rechazar con un 403.
// ============================================================
import { NavLink, Outlet } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';

/** Qué secciones ve cada rol (RF-14). */
const SECCIONES = [
  { ruta: '/panel/agenda', titulo: 'Agenda',
    roles: ['ADMIN', 'RECEPCION', 'VETERINARIO', 'ESTETICISTA'] },
  { ruta: '/panel/historia', titulo: 'Historia clínica',
    roles: ['ADMIN', 'VETERINARIO'] },
  { ruta: '/panel/clientes', titulo: 'Clientes y pacientes',
    roles: ['ADMIN', 'RECEPCION', 'VETERINARIO'] },
  { ruta: '/panel/inventario', titulo: 'Inventario',
    roles: ['ADMIN', 'RECEPCION'] },
  { ruta: '/panel/servicios', titulo: 'Servicios',
    roles: ['ADMIN'] }
];

const NOMBRE_ROL = {
  ADMIN: 'Administración',
  RECEPCION: 'Recepción',
  VETERINARIO: 'Veterinaria',
  ESTETICISTA: 'Estética y spa'
};

export default function PanelLayout() {
  const { usuario } = useAuth();
  const visibles = SECCIONES.filter((s) => s.roles.includes(usuario.rol));

  return (
    <div className="panel">
      <header className="panel-cabecera">
        <div>
          <p className="panel-rotulo">Panel interno</p>
          <h1 className="panel-titulo">{usuario.nombre}</h1>
        </div>
        <span className="panel-rol">{NOMBRE_ROL[usuario.rol] || usuario.rol}</span>
      </header>

      <nav className="panel-nav" aria-label="Secciones del panel">
        {visibles.map((s) => (
          <NavLink key={s.ruta} to={s.ruta}
                   className={({ isActive }) => isActive ? 'panel-tab activa' : 'panel-tab'}>
            {s.titulo}
          </NavLink>
        ))}
      </nav>

      <div className="panel-contenido">
        <Outlet />
      </div>
    </div>
  );
}
