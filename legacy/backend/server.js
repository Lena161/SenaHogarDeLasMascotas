const path = require('path');
const express = require('express');
const session = require('express-session');
const bcrypt = require('bcryptjs');
const { readDb, writeDb } = require('./db');

const app = express();
const PORT = process.env.PORT || 3000;
const FRONTEND_DIR = path.join(__dirname, '..', 'frontend');

app.use(express.json());
app.use(express.static(FRONTEND_DIR));
app.use(
  session({
    secret: 'hogar-de-las-mascotas-secret-local',
    resave: false,
    saveUninitialized: false,
    cookie: { maxAge: 1000 * 60 * 60 * 8 } // 8 horas
  })
);

function requireAuth(req, res, next) {
  if (!req.session.userId) {
    return res.status(401).json({ error: 'Debes iniciar sesión.' });
  }
  next();
}

// ---------- AUTENTICACIÓN ----------

app.post('/api/register', (req, res) => {
  const { nombre, email, password } = req.body || {};
  if (!nombre || !email || !password) {
    return res.status(400).json({ error: 'Completa todos los campos.' });
  }
  if (password.length < 6) {
    return res.status(400).json({ error: 'La contraseña debe tener al menos 6 caracteres.' });
  }
  const db = readDb();
  const emailNorm = String(email).trim().toLowerCase();
  if (db.users.find((u) => u.email === emailNorm)) {
    return res.status(409).json({ error: 'Ya existe una cuenta con ese correo.' });
  }
  const user = {
    id: db.nextUserId++,
    nombre: String(nombre).trim(),
    email: emailNorm,
    passwordHash: bcrypt.hashSync(password, 10),
    createdAt: new Date().toISOString()
  };
  db.users.push(user);
  writeDb(db);
  req.session.userId = user.id;
  res.json({ id: user.id, nombre: user.nombre, email: user.email });
});

app.post('/api/login', (req, res) => {
  const { email, password } = req.body || {};
  if (!email || !password) {
    return res.status(400).json({ error: 'Completa todos los campos.' });
  }
  const db = readDb();
  const emailNorm = String(email).trim().toLowerCase();
  const user = db.users.find((u) => u.email === emailNorm);
  if (!user || !bcrypt.compareSync(password, user.passwordHash)) {
    return res.status(401).json({ error: 'Correo o contraseña incorrectos.' });
  }
  req.session.userId = user.id;
  res.json({ id: user.id, nombre: user.nombre, email: user.email });
});

app.post('/api/logout', (req, res) => {
  req.session.destroy(() => res.json({ ok: true }));
});

app.get('/api/me', (req, res) => {
  if (!req.session.userId) return res.json(null);
  const db = readDb();
  const user = db.users.find((u) => u.id === req.session.userId);
  if (!user) return res.json(null);
  res.json({ id: user.id, nombre: user.nombre, email: user.email });
});

// ---------- CITAS ----------

const SERVICIOS = [
  'Consulta Veterinaria',
  'Vacunación',
  'Desparasitación',
  'Atención de Urgencias No Vitales',
  'Baños Medicados',
  'Spa y Peluquería',
  'Cirugías de Esterilización',
  'Profilaxis Dental',
  'Exámenes de Laboratorio'
];

app.get('/api/servicios', (req, res) => res.json(SERVICIOS));

app.post('/api/citas', requireAuth, (req, res) => {
  const { mascota, servicio, fecha, hora, notas } = req.body || {};
  if (!mascota || !servicio || !fecha || !hora) {
    return res.status(400).json({ error: 'Completa mascota, servicio, fecha y hora.' });
  }
  const db = readDb();
  const cita = {
    id: db.nextCitaId++,
    userId: req.session.userId,
    mascota: String(mascota).trim(),
    servicio,
    fecha,
    hora,
    notas: String(notas || '').trim(),
    estado: 'pendiente',
    createdAt: new Date().toISOString()
  };
  db.citas.push(cita);
  writeDb(db);
  res.status(201).json(cita);
});

app.get('/api/citas', requireAuth, (req, res) => {
  const db = readDb();
  const citas = db.citas
    .filter((c) => c.userId === req.session.userId)
    .sort((a, b) => (a.fecha + a.hora).localeCompare(b.fecha + b.hora));
  res.json(citas);
});

app.delete('/api/citas/:id', requireAuth, (req, res) => {
  const db = readDb();
  const id = parseInt(req.params.id, 10);
  const cita = db.citas.find((c) => c.id === id);
  if (!cita || cita.userId !== req.session.userId) {
    return res.status(404).json({ error: 'Cita no encontrada.' });
  }
  db.citas = db.citas.filter((c) => c.id !== id);
  writeDb(db);
  res.json({ ok: true });
});

// ---------- CONTACTO ----------

app.post('/api/contacto', (req, res) => {
  const { nombre, email, mensaje } = req.body || {};
  if (!nombre || !email || !mensaje) {
    return res.status(400).json({ error: 'Completa todos los campos.' });
  }
  const db = readDb();
  const registro = {
    id: db.nextMensajeId++,
    nombre: String(nombre).trim(),
    email: String(email).trim().toLowerCase(),
    mensaje: String(mensaje).trim(),
    createdAt: new Date().toISOString()
  };
  db.mensajes.push(registro);
  writeDb(db);
  res.status(201).json({ ok: true });
});

app.listen(PORT, () => {
  console.log(`Servidor corriendo en http://localhost:${PORT}`);
  console.log(`Sirviendo frontend desde: ${FRONTEND_DIR}`);
});
