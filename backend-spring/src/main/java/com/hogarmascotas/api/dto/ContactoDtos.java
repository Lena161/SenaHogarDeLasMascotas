package com.hogarmascotas.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class ContactoDtos {
    public record ContactoRequest(
            @NotBlank String nombre,
            @NotBlank @Email String email,
            @NotBlank String mensaje
    ) {}
}
