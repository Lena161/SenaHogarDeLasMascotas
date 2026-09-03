// Rutas de la SPA — CONMUTADO: se agrega /mascotas (protegida),
// la página nueva que exige el modelo definitivo (RF-02).
import { BrowserRouter, Routes, Route } from 'react-router-dom';
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
            <RutaProtegida><Mascotas /></RutaProtegida>
          } />
          <Route path="/agendar" element={
            <RutaProtegida><Agendar /></RutaProtegida>
          } />
        </Routes>
        <Footer />
      </BrowserRouter>
    </AuthProvider>
  );
}

