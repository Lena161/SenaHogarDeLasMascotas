const fs = require('fs');
const path = require('path');

const DB_PATH = path.join(__dirname, 'data', 'db.json');

function ensureDb() {
  const dir = path.dirname(DB_PATH);
  if (!fs.existsSync(dir)) fs.mkdirSync(dir, { recursive: true });
  if (!fs.existsSync(DB_PATH)) {
    fs.writeFileSync(
      DB_PATH,
      JSON.stringify(
        { users: [], citas: [], mensajes: [], nextUserId: 1, nextCitaId: 1, nextMensajeId: 1 },
        null,
        2
      )
    );
  }
}

function readDb() {
  ensureDb();
  const db = JSON.parse(fs.readFileSync(DB_PATH, 'utf-8'));
  // Compatibilidad con bases de datos creadas antes de agregar "mensajes"
  if (!db.mensajes) db.mensajes = [];
  if (!db.nextMensajeId) db.nextMensajeId = 1;
  return db;
}

function writeDb(data) {
  fs.writeFileSync(DB_PATH, JSON.stringify(data, null, 2));
}

module.exports = { readDb, writeDb };
