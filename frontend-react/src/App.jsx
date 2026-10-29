// Rutas de la SPA — CONMUTADO: se agrega /mascotas (protegida),
// la página nueva que exige el modelo definitivo (RF-02).
//
// Ampliación: el panel del personal cuelga de /panel con rutas
// anidadas. Cada sección declara qué roles la ven; el backend
// vuelve a verificarlo, porque esta capa es solo de navegación.
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Header from './components/Header';
import Footer from './components/Footer';
import RutaProtegida from './components/RutaProtegida';
import Inicio from './pages/Inicio';
import Servicios from './pages/Servicios';
import Productos from './pages/Productos';
import Nosotros from './pages/Nosotros';
import Contacto from './pages/Contacto';
import Ingresar from './pages/Ingresar';
import Agendar from './pages/Agendar';
import Mascotas from './pages/Mascotas';
import PanelLayout from './pages/panel/PanelLayout';
import PanelAgenda from './pages/panel/Agenda';
import PanelClientes from './pages/panel/Clientes';
import PanelHistoria from './pages/panel/HistoriaClinica';
import PanelInventario from './pages/panel/Inventario';
import PanelServicios from './pages/panel/Servicios';

const STAFF = ['ADMIN', 'RECEPCION', 'VETERINARIO', 'ESTETICISTA'];

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Header />
        <Routes>
          <Route path="/" element={<Inicio />} />
          <Route path="/servicios" element={<Servicios />} />
          <Route path="/productos" element={<Productos />} />
          <Route path="/nosotros" element={<Nosotros />} />
          <Route path="/contacto" element={<Contacto />} />
          <Route path="/ingresar" element={<Ingresar />} />
          <Route path="/mascotas" element={
            <RutaProtegida roles={['CLIENTE']}><Mascotas /></RutaProtegida>
          } />
          <Route path="/agendar" element={
            <RutaProtegida><Agendar /></RutaProtegida>
          } />

          {/* Panel del personal (RF-03, RF-04, RF-07..RF-13) */}
          <Route path="/panel" element={
            <RutaProtegida roles={STAFF}><PanelLayout /></RutaProtegida>
          }>
            <Route index element={<Navigate to="/panel/agenda" replace />} />
            <Route path="agenda" element={<PanelAgenda />} />
            <Route path="historia" element={
              <RutaProtegida roles={['ADMIN', 'VETERINARIO']}>
                <PanelHistoria />
              </RutaProtegida>
            } />
            <Route path="clientes" element={<PanelClientes />} />
            <Route path="inventario" element={<PanelInventario />} />
            <Route path="servicios" element={<PanelServicios />} />
          </Route>
        </Routes>
        <Footer />
      </BrowserRouter>
    </AuthProvider>
  );
}
