package com.hogarmascotas.api.controller;

import com.hogarmascotas.api.dto.ContactoDtos.ContactoRequest;
import com.hogarmascotas.api.entity.MensajeContacto;
import com.hogarmascotas.api.repository.MensajeContactoRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/** Formulario publico de contacto (paridad con el prototipo Express). */
@RestController
@RequestMapping("/api/v1/contacto")
public class ContactoController {

    private final MensajeContactoRepository mensajeRepo;

    public ContactoController(MensajeContactoRepository mensajeRepo) {
        this.mensajeRepo = mensajeRepo;
    }

    @PostMapping
    public ResponseEntity<Map<String, Boolean>> enviar(@Valid @RequestBody ContactoRequest datos) {
        MensajeContacto mensaje = new MensajeContacto();
        mensaje.setNombre(datos.nombre().trim());
        mensaje.setEmail(datos.email().trim().toLowerCase());
        mensaje.setMensaje(datos.mensaje().trim());
        mensajeRepo.save(mensaje);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("ok", true));
    }
}
