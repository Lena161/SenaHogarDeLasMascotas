// Actualiza el header en todas las páginas según si hay una sesión activa.
(function () {
  function escapeHtml(str) {
    const div = document.createElement('div');
    div.textContent = str;
    return div.innerHTML;
  }

  async function actualizarHeader() {
    const cont = document.getElementById('header-actions');
    if (!cont) return;
    try {
      const res = await fetch('/api/me');
      const user = await res.json();
      if (user) {
        cont.innerHTML = `
          <span class="user-greeting">Hola, ${escapeHtml(user.nombre.split(' ')[0])}</span>
          <a href="agendar.html" class="btn-solid">Agendar Cita</a>
          <button id="btn-logout" class="btn-outline" type="button">Cerrar sesión</button>
        `;
        const btn = document.getElementById('btn-logout');
        if (btn) {
          btn.addEventListener('click', async () => {
            await fetch('/api/logout', { method: 'POST' });
            window.location.href = 'index.html';
          });
        }
      }
    } catch (e) {
      // El servidor no está disponible; se deja el header por defecto.
    }
  }

  document.addEventListener('DOMContentLoaded', actualizarHeader);
})();
