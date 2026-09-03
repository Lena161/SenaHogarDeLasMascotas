package com.hogarmascotas.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * PATRON DTO: las entidades JPA nunca viajan por la red. Estos records
 * definen el contrato exacto de entrada/salida de la API: controlan que
 * datos se exponen (el hash de contrasena jamas sale) y que datos se
 * aceptan (validaciones con anotaciones).
 */
public class AuthDtos {

    public record RegistroRequest(
            @NotBlank String nombres,
            @NotBlank String apellidos,
            @NotBlank String numeroDocumento,
            @NotBlank String telefono,
            @NotBlank @Email String correo,
            @NotBlank @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres") String password
    ) {}

    public record LoginRequest(
            @NotBlank String username,
            @NotBlank String password
    ) {}

    public record SesionResponse(
            Long id,
            String nombre,
            String username,
            String rol,
            String token
    ) {}
}
