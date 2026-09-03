// Header — CONMUTADO: agrega el enlace "Mis Mascotas" para clientes
// (la mascota ahora es una entidad, no un texto en el formulario)
// y el logout ya no llama al servidor (JWT sin estado).
import { NavLink, Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export default function Header() {
  const { usuario, cerrarSesion } = useAuth();
  const navegar = useNavigate();

  function manejarLogout() {
    cerrarSesion();
    navegar('/');
  }

  return (
    <header className="site-header">
      <Link to="/" className="logo">🐾 Hogar de Las Mascotas</Link>

      <nav>
        <NavLink to="/" end>Inicio</NavLink>
        <NavLink to="/productos">Productos</NavLink>
        <NavLink to="/servicios">Servicios</NavLink>
        <NavLink to="/nosotros">Nosotros</NavLink>
        <NavLink to="/contacto">Contacto</NavLink>
        {usuario && usuario.rol === 'CLIENTE' && (
          <NavLink to="/mascotas">Mis Mascotas</NavLink>
        )}
      </nav>

      <div className="header-actions">
        {usuario ? (
          <>
            <span className="user-greeting">Hola, {usuario.nombre.split(' ')[0]}</span>
            <Link to="/agendar" className="btn-solid">Agendar Cita</Link>
            <button type="button" className="btn-outline" onClick={manejarLogout}>
              Cerrar sesión
            </button>
          </>
        ) : (
          <>
            <Link to="/ingresar" className="btn-outline">Ingresar</Link>
            <Link to="/agendar" className="btn-solid">Agendar Cita</Link>
          </>
        )}
      </div>
    </header>
  );
}
