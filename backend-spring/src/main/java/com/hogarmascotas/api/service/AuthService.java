package com.hogarmascotas.api.service;

import com.hogarmascotas.api.config.JwtUtil;
import com.hogarmascotas.api.dto.AuthDtos.*;
import com.hogarmascotas.api.entity.Dueno;
import com.hogarmascotas.api.entity.Usuario;
import com.hogarmascotas.api.exception.RecursoNoEncontradoException;
import com.hogarmascotas.api.exception.ReglaNegocioException;
import com.hogarmascotas.api.repository.DuenoRepository;
import com.hogarmascotas.api.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final DuenoRepository duenoRepo;
    private final PasswordEncoder codificador;
    private final JwtUtil jwtUtil;

    // Inyeccion por constructor: dependencias explicitas y probables con mocks
    public AuthService(UsuarioRepository usuarioRepo, DuenoRepository duenoRepo,
                       PasswordEncoder codificador, JwtUtil jwtUtil) {
        this.usuarioRepo = usuarioRepo;
        this.duenoRepo = duenoRepo;
        this.codificador = codificador;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registro publico de clientes: crea el Dueno (dominio) y su Usuario
     * (credenciales) en una sola transaccion. Las cuentas del personal
     * las crea el ADMIN (UC14), nunca este endpoint.
     */
    @Transactional
    public SesionResponse registrarCliente(RegistroRequest datos) {
        if (duenoRepo.existsByCorreo(datos.correo()) || usuarioRepo.existsByUsername(datos.correo())) {
            throw new ReglaNegocioException("Ya existe una cuenta con ese correo.");
        }
        if (duenoRepo.existsByNumeroDocumento(datos.numeroDocumento())) {
            throw new ReglaNegocioException("Ya existe un cliente con ese documento.");
        }

        Dueno dueno = new Dueno();
        dueno.setNombres(datos.nombres().trim());
        dueno.setApellidos(datos.apellidos().trim());
        dueno.setNumeroDocumento(datos.numeroDocumento().trim());
        dueno.setTelefono(datos.telefono().trim());
        dueno.setCorreo(datos.correo().trim().toLowerCase());
        duenoRepo.save(dueno);

        Usuario usuario = new Usuario();
        usuario.setUsername(dueno.getCorreo()); // el correo actua como username
        usuario.setPasswordHash(codificador.encode(datos.password())); // RNF-03
        usuario.setRol(Usuario.Rol.CLIENTE);
        usuario.setDueno(dueno);
        usuarioRepo.save(usuario);

        String token = jwtUtil.generar(usuario.getUsername(), usuario.getRol().name());
        return new SesionResponse(usuario.getId(), dueno.getNombreCompleto(),
                usuario.getUsername(), usuario.getRol().name(), token);
    }

    public SesionResponse iniciarSesion(LoginRequest datos) {
        Usuario usuario = usuarioRepo.findByUsername(datos.username().trim().toLowerCase())
                .filter(Usuario::isActivo)
                // Mensaje generico deliberado (buena practica conservada del
                // prototipo): no se revela cual campo fallo.
                .orElseThrow(() -> new ReglaNegocioException("Usuario o contrasena incorrectos."));

        if (!codificador.matches(datos.password(), usuario.getPasswordHash())) {
            throw new ReglaNegocioException("Usuario o contrasena incorrectos.");
        }

        // Guard defensivo: si por un dato anomalo la cuenta no tiene
        // vinculo (la BD lo impide con chk_usuario_vinculo, pero la
        // defensa en profundidad aplica tambien en el codigo), se usa
        // el username en lugar de lanzar un NullPointerException.
        String nombre;
        if (usuario.getDueno() != null) {
            nombre = usuario.getDueno().getNombreCompleto();
        } else if (usuario.getEmpleado() != null) {
            nombre = usuario.getEmpleado().getNombreCompleto();
        } else {
            nombre = usuario.getUsername();
        }
        String token = jwtUtil.generar(usuario.getUsername(), usuario.getRol().name());
        return new SesionResponse(usuario.getId(), nombre,
                usuario.getUsername(), usuario.getRol().name(), token);
    }

    /** Usuario autenticado actual (a partir del username del token). */
    public Usuario obtenerPorUsername(String username) {
        return usuarioRepo.findByUsername(username)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado."));
    }
}

