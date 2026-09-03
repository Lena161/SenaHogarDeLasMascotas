// Migración de contacto.html: información + formulario contra la API.
// Antes: listeners manuales y showMsg(); ahora: estado con useState.
import { useState } from 'react';
import Hero from '../components/Hero';
import Panel from '../components/Panel';
import FormMessage from '../components/FormMessage';
import { api } from '../services/api';
import { IconoWhatsApp, IconoCorreo, IconoUbicacion, IconoReloj } from '../components/Iconos';
import { WHATSAPP, CORREO, DIRECCION, HORARIO, enlaceWhatsApp } from '../data/negocio';

export default function Contacto() {
  const [formulario, setFormulario] = useState({ nombre: '', email: '', mensaje: '' });
  const [mensaje, setMensaje] = useState(null);
  const [enviando, setEnviando] = useState(false);

  function manejarCambio(e) {
    setFormulario({ ...formulario, [e.target.name]: e.target.value });
  }

  async function manejarEnvio(e) {
    e.preventDefault();
    setEnviando(true);
    try {
      await api.enviarContacto(formulario);
      setMensaje({ tipo: 'success', texto: '¡Gracias! Recibimos tu mensaje y te responderemos pronto.' });
      setFormulario({ nombre: '', email: '', mensaje: '' });
    } catch (error) {
      setMensaje({ tipo: 'error', texto: error.message });
    } finally {
      setEnviando(false);
    }
  }

  return (
    <main>
      <Hero titulo="Contáctanos"
            subtitulo="Estamos para ayudarte a ti y a tu mascota. Escríbenos por el canal que prefieras." />

      <div className="contacto-grid">
        <Panel titulo="Información de Contacto">
          <div className="info-list">
            <div className="info-item">
              <div className="icon-wrap"><IconoWhatsApp /></div>
              <div>
                <strong>WhatsApp</strong>
                <span><a href={enlaceWhatsApp('Hola, quiero más información')} target="_blank" rel="noreferrer">{WHATSAPP}</a></span>
              </div>
            </div>
            <div className="info-item">
              <div className="icon-wrap"><IconoCorreo /></div>
              <div>
                <strong>Correo Electrónico</strong>
                <span><a href={`mailto:${CORREO}`}>{CORREO}</a></span>
              </div>
            </div>
            <div className="info-item">
              <div className="icon-wrap"><IconoUbicacion /></div>
              <div>
                <strong>Dirección</strong>
                <span>{DIRECCION}</span>
              </div>
            </div>
            <div className="info-item">
              <div className="icon-wrap"><IconoReloj /></div>
              <div>
                <strong>Horario de Atención</strong>
                <span>{HORARIO}</span>
              </div>
            </div>
          </div>
        </Panel>

        <Panel titulo="Escríbenos">
          <form className="form-app" onSubmit={manejarEnvio}>
            <FormMessage mensaje={mensaje} />
            <label>Nombre
              <input type="text" name="nombre" required autoComplete="name"
                     value={formulario.nombre} onChange={manejarCambio} />
            </label>
            <label>Correo electrónico
              <input type="email" name="email" required autoComplete="email"
                     value={formulario.email} onChange={manejarCambio} />
            </label>
            <label>Mensaje
              <textarea name="mensaje" required placeholder="Cuéntanos en qué te podemos ayudar"
                        value={formulario.mensaje} onChange={manejarCambio}
                        style={{ minHeight: '110px' }} />
            </label>
            <button type="submit" className="form-submit" disabled={enviando}>
              {enviando ? 'Enviando…' : 'Enviar Mensaje'}
            </button>
          </form>
        </Panel>
      </div>
    </main>
  );
}
