// Ingresar — CONMUTADO al contrato definitivo:
// - El registro ahora crea un Dueño real: pide nombres, apellidos,
//   documento y teléfono (antes: solo nombre, correo, contraseña).
// - El login envía { username, password }: el username es el correo.
import { useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import FormMessage from '../components/FormMessage';
import { useAuth } from '../context/AuthContext';

export default function Ingresar() {
  const { iniciarSesion, registrarse } = useAuth();
  const navegar = useNavigate();
  const [parametros] = useSearchParams();
  const destino = parametros.get('redirect') || '/agendar';

  const [pestana, setPestana] = useState('login');
  const [mensaje, setMensaje] = useState(null);
  const [login, setLogin] = useState({ username: '', password: '' });
  const [registro, setRegistro] = useState({
    nombres: '', apellidos: '', numeroDocumento: '',
    telefono: '', correo: '', password: ''
  });

  async function manejarLogin(e) {
    e.preventDefault();
    try {
      await iniciarSesion(login);
      navegar(destino);
    } catch (error) {
      setMensaje({ tipo: 'error', texto: error.message });
    }
  }

  async function manejarRegistro(e) {
    e.preventDefault();
    try {
      await registrarse(registro);
      navegar(destino);
    } catch (error) {
      setMensaje({ tipo: 'error', texto: error.message });
    }
  }

  function cambiarPestana(nombre) {
    setPestana(nombre);
    setMensaje(null);
  }

  function cambioRegistro(e) {
    setRegistro({ ...registro, [e.target.name]: e.target.value });
  }

  return (
    <main>
      <div className="auth-wrap">
        <div className="auth-card">
          <div className="auth-tabs">
            <button type="button"
                    className={pestana === 'login' ? 'active' : ''}
                    onClick={() => cambiarPestana('login')}>
              Ingresar
            </button>
            <button type="button"
                    className={pestana === 'registro' ? 'active' : ''}
                    onClick={() => cambiarPestana('registro')}>
              Crear cuenta
            </button>
          </div>

          {pestana === 'login' ? (
            <form className="form-app" onSubmit={manejarLogin}>
              <FormMessage mensaje={mensaje} />
              <label>Correo electrónico
                <input type="email" required autoComplete="email"
                       value={login.username}
                       onChange={(e) => setLogin({ ...login, username: e.target.value })} />
              </label>
              <label>Contraseña
                <input type="password" required autoComplete="current-password"
                       value={login.password}
                       onChange={(e) => setLogin({ ...login, password: e.target.value })} />
              </label>
              <button type="submit" className="form-submit">Ingresar</button>
            </form>
          ) : (
            <form className="form-app" onSubmit={manejarRegistro}>
              <FormMessage mensaje={mensaje} />
              <div className="form-row">
                <label>Nombres
                  <input type="text" name="nombres" required
                         value={registro.nombres} onChange={cambioRegistro} />
                </label>
                <label>Apellidos
                  <input type="text" name="apellidos" required
                         value={registro.apellidos} onChange={cambioRegistro} />
                </label>
              </div>
              <div className="form-row">
                <label>Documento
                  <input type="text" name="numeroDocumento" required
                         value={registro.numeroDocumento} onChange={cambioRegistro} />
                </label>
                <label>Teléfono
                  <input type="tel" name="telefono" required
                         value={registro.telefono} onChange={cambioRegistro} />
                </label>
              </div>
              <label>Correo electrónico
                <input type="email" name="correo" required autoComplete="email"
                       value={registro.correo} onChange={cambioRegistro} />
              </label>
              <label>Contraseña (mínimo 6 caracteres)
                <input type="password" name="password" required minLength={6}
                       autoComplete="new-password"
                       value={registro.password} onChange={cambioRegistro} />
              </label>
              <button type="submit" className="form-submit">Crear cuenta</button>
            </form>
          )}
        </div>
      </div>
    </main>
  );
}
