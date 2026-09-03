// ============================================================
// Iconos SVG portados tal cual del código base (mismos paths).
// Antes: SVG repetidos en línea dentro de cada HTML.
// Ahora: componentes reutilizables; el color y tamaño los
// controla el CSS del contenedor (.icon-wrap), como antes.
// ============================================================

function SvgBase({ children, relleno = 'none' }) {
  return (
    <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24"
         fill={relleno} stroke="currentColor" strokeWidth="2"
         strokeLinecap="round" strokeLinejoin="round">
      <path stroke="none" d="M0 0h24v24H0z" fill="none" />
      {children}
    </svg>
  );
}

export const IconoConsulta = () => (
  <SvgBase>
    <path d="M6 4h-1a2 2 0 0 0 -2 2v3.5h0a5.5 5.5 0 0 0 11 0v-3.5a2 2 0 0 0 -2 -2h-1" />
    <path d="M8 15a6 6 0 1 0 12 0v-3" />
    <path d="M11 3v2" /><path d="M6 3v2" />
    <path d="M20 10m-2 0a2 2 0 1 0 4 0a2 2 0 1 0 -4 0" />
  </SvgBase>
);

export const IconoSpa = () => (
  <SvgBase>
    <path d="M6 7m-3 0a3 3 0 1 0 6 0a3 3 0 1 0 -6 0" />
    <path d="M6 17m-3 0a3 3 0 1 0 6 0a3 3 0 1 0 -6 0" />
    <path d="M8.6 8.6l10.4 10.4" />
    <path d="M8.6 15.4l10.4 -10.4" />
  </SvgBase>
);

export const IconoVacuna = () => (
  <SvgBase>
    <path d="M4.5 20.5l4 -4" />
    <path d="M8.5 16.5l5.5 -5.5" />
    <path d="M17 3l4 4" />
    <path d="M17 3l-2 2l1 1l-2 2l-1 -1l-2.5 2.5l3 3l2.5 -2.5l-1 -1l2 -2l1 1l2 -2z" />
    <path d="M9.5 11.5l3 3" />
  </SvgBase>
);

export const IconoBano = () => (
  <SvgBase>
    <path d="M6 20h-2a2 2 0 0 1 -2 -2v-6h20v6a2 2 0 0 1 -2 2h-2" />
    <path d="M8 20h8" />
    <path d="M2 12v-5a2 2 0 0 1 2 -2h1v-1a1 1 0 0 1 2 0v1h6v-1a1 1 0 0 1 2 0v1h1a2 2 0 0 1 2 2v5" />
  </SvgBase>
);

export const IconoUrgencia = () => (
  <SvgBase>
    <path d="M10 10m-7 0a7 7 0 1 0 14 0a7 7 0 1 0 -14 0" />
    <path d="M7 10h6" /><path d="M10 7v6" />
    <path d="M21 21l-6 -6" />
  </SvgBase>
);

export const IconoDesparasitacion = () => (
  <SvgBase>
    <path d="M4.5 9.5v-2a2 2 0 0 1 4 0v2" />
    <path d="M4.5 9.5h4" /><path d="M6.5 9.5v8.5" />
    <path d="M10 12h2" /><path d="M10 16h2" />
    <path d="M13.5 9.5a2 2 0 0 1 4 0v2h-4v-2z" />
    <path d="M13.5 9.5h4" /><path d="M15.5 11.5v7" />
    <path d="M9 18a1 1 0 0 0 -1 1v1h8v-1a1 1 0 0 0 -1 -1h-6z" />
  </SvgBase>
);

export const IconoCirugia = () => (
  <SvgBase>
    <path d="M8 21l8 0" /><path d="M12 17l0 4" />
    <path d="M7 4l0 4" /><path d="M17 4l0 4" />
    <path d="M6 8m0 2a2 2 0 0 1 2 -2h8a2 2 0 0 1 2 2v1a2 2 0 0 1 -2 2h-8a2 2 0 0 1 -2 -2z" />
    <path d="M12 13l0 4" />
  </SvgBase>
);

export const IconoDental = () => (
  <SvgBase>
    <path d="M12 5.5m-2.5 0a2.5 2.5 0 1 0 5 0a2.5 2.5 0 1 0 -5 0" />
    <path d="M7 14.5c0 -2.5 2.239 -4.5 5 -4.5s5 2 5 4.5" />
    <path d="M5 21v-1a2 2 0 0 1 2 -2h10a2 2 0 0 1 2 2v1" />
    <path d="M9 17l1.5 2l1.5 -2l1.5 2l1.5 -2" />
  </SvgBase>
);

export const IconoLaboratorio = () => (
  <SvgBase>
    <path d="M9 3l0 7l-4.5 7.5a1 1 0 0 0 .9 1.5h13.2a1 1 0 0 0 .9 -1.5l-4.5 -7.5v-7" />
    <path d="M6.5 3h11" />
    <path d="M10 14l1.5 -1.5l1.5 1.5l1.5 -1.5" />
  </SvgBase>
);

export const IconoTienda = () => (
  <SvgBase relleno="currentColor">
    <path d="M6 2a1 1 0 0 1 .993 .883l.007 .117v1.068l13.071 .935a1 1 0 0 1 .929 1.024l-.01 .114l-1 7a1 1 0 0 1 -.877 .853l-.113 .006h-12v2h10a3 3 0 1 1 -2.995 3.176l-.005 -.176l.005 -.176c.017 -.288 .074 -.564 .166 -.824h-5.342a3 3 0 1 1 -5.824 1.176l-.005 -.176l.005 -.176a3.002 3.002 0 0 1 1.995 -2.654v-12.17h-1a1 1 0 0 1 -.993 -.883l-.007 -.117a1 1 0 0 1 .883 -.993l.117 -.007h2zm0 16a1 1 0 1 0 0 2a1 1 0 0 0 0 -2zm11 0a1 1 0 1 0 0 2a1 1 0 0 0 0 -2z" />
  </SvgBase>
);

export const IconoCorazon = () => (
  <SvgBase>
    <path d="M19.5 12.572l-7.5 7.428l-7.5 -7.428a5 5 0 1 1 7.5 -6.566a5 5 0 1 1 7.5 6.572" />
  </SvgBase>
);

export const IconoEscudo = () => (
  <SvgBase>
    <path d="M12 3a12 12 0 0 0 8.5 3a12 12 0 0 1 -8.5 15a12 12 0 0 1 -8.5 -15a12 12 0 0 0 8.5 -3" />
  </SvgBase>
);

export const IconoReloj = () => (
  <SvgBase>
    <path d="M12 12m-9 0a9 9 0 1 0 18 0a9 9 0 1 0 -18 0" />
    <path d="M12 7v5l3 3" />
  </SvgBase>
);

export const IconoCheck = () => (
  <SvgBase>
    <path d="M5 12l5 5l10 -10" />
  </SvgBase>
);

export const IconoCorreo = () => (
  <SvgBase>
    <path d="M3 7a2 2 0 0 1 2 -2h14a2 2 0 0 1 2 2v10a2 2 0 0 1 -2 2h-14a2 2 0 0 1 -2 -2v-10z" />
    <path d="M3 7l9 6l9 -6" />
  </SvgBase>
);

export const IconoUbicacion = () => (
  <SvgBase>
    <path d="M9 11a3 3 0 1 0 6 0a3 3 0 0 0 -6 0" />
    <path d="M17.657 16.657l-4.243 4.243a2 2 0 0 1 -2.827 0l-4.244 -4.243a8 8 0 1 1 11.314 0z" />
  </SvgBase>
);

export const IconoWhatsApp = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="white">
    <path d="M17.472 14.382c-.297-.149-1.758-.867-2.03-.967-.273-.099-.471-.148-.67.15-.197.297-.767.966-.94 1.164-.173.199-.347.223-.644.075-.297-.15-1.255-.463-2.39-1.475-.883-.788-1.48-1.761-1.653-2.059-.173-.297-.018-.458.13-.606.134-.133.298-.347.446-.52.149-.174.198-.298.298-.497.099-.198.05-.371-.025-.52-.075-.149-.669-1.612-.916-2.207-.242-.579-.487-.5-.669-.51-.173-.008-.371-.01-.57-.01-.198 0-.52.074-.792.372-.272.297-1.04 1.016-1.04 2.479 0 1.462 1.065 2.875 1.213 3.074.149.198 2.096 3.2 5.077 4.487.709.306 1.262.489 1.694.626.712.226 1.36.194 1.872.118.571-.085 1.758-.719 2.006-1.413.248-.694.248-1.289.173-1.413-.074-.124-.272-.198-.57-.347z"/>
    <path d="M12 21a9 9 0 1 1 8.94-10.06L21 21l-6.06-.94A9 9 0 0 1 12 21z" fill="none" stroke="white" strokeWidth="1.3"/>
  </svg>
);

export const IconoBusqueda = () => (
  <svg xmlns="http://www.w3.org/2000/svg" width="22" height="22" viewBox="0 0 24 24" fill="currentColor">
    <path stroke="none" d="M0 0h24v24H0z" fill="none"/>
    <path d="M12 10c-1.32 0 -1.983 .421 -2.931 1.924l-.244 .398l-.395 .688a50.89 50.89 0 0 0 -.141 .254c-.24 .434 -.571 .753 -1.139 1.142l-.55 .365c-.94 .627 -1.432 1.118 -1.707 1.955c-.124 .338 -.196 .853 -.193 1.28c0 1.687 1.198 2.994 2.8 2.994l.242 -.006c.119 -.006 .234 -.017 .354 -.034l.248 -.043l.132 -.028l.291 -.073l.162 -.045l.57 -.17l.763 -.243l.455 -.136c.53 -.15 .94 -.222 1.283 -.222c.344 0 .753 .073 1.283 .222l.455 .136l.764 .242l.569 .171l.312 .084c.097 .024 .187 .045 .273 .062l.248 .043c.12 .017 .235 .028 .354 .034l.242 .006c1.602 0 2.8 -1.307 2.8 -3c0 -.427 -.073 -.939 -.207 -1.306c-.236 -.724 -.677 -1.223 -1.48 -1.83l-.257 -.19l-.528 -.38c-.642 -.47 -1.003 -.826 -1.253 -1.278l-.27 -.485l-.252 -.432c-1.011 -1.696 -1.618 -2.099 -3.053 -2.099z" />
    <path d="M19.78 7h-.03c-1.219 .02 -2.35 1.066 -2.908 2.504c-.69 1.775 -.348 3.72 1.075 4.333c.256 .109 .527 .163 .801 .163c1.231 0 2.38 -1.053 2.943 -2.504c.686 -1.774 .34 -3.72 -1.076 -4.332a2.05 2.05 0 0 0 -.804 -.164z" />
    <path d="M9.025 3c-.112 0 -.185 .002 -.27 .015l-.093 .016c-1.532 .206 -2.397 1.989 -2.108 3.855c.272 1.725 1.462 3.114 2.92 3.114l.187 -.005a1.26 1.26 0 0 0 .084 -.01l.092 -.016c1.533 -.206 2.397 -1.989 2.108 -3.855c-.27 -1.727 -1.46 -3.114 -2.92 -3.114z" />
    <path d="M14.972 3c-1.459 0 -2.647 1.388 -2.916 3.113c-.29 1.867 .574 3.65 2.174 3.867c.103 .013 .2 .02 .296 .02c1.39 0 2.543 -1.265 2.877 -2.883l.041 -.23c.29 -1.867 -.574 -3.65 -2.174 -3.867a2.154 2.154 0 0 0 -.298 -.02z" />
    <path d="M4.217 7c-.274 0 -.544 .054 -.797 .161c-1.426 .615 -1.767 2.562 -1.078 4.335c.563 1.451 1.71 2.504 2.941 2.504c.274 0 .544 -.054 .797 -.161c1.426 -.615 1.767 -2.562 1.078 -4.335c-.563 -1.451 -1.71 -2.504 -2.941 -2.504z" />
  </svg>
);
