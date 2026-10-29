package com.hogarmascotas.api.dto;

import com.hogarmascotas.api.dto.MascotaDtos.MascotaResponse;
import com.hogarmascotas.api.entity.Dueno;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/** Contrato de la gestion de clientes por el personal (RF-03). */
public class DuenoDtos {

    public record DuenoResponse(
            Long id, String tipoDocumento, String numeroDocumento,
            String nombres, String apellidos, String nombreCompleto,
            String telefono, String correo, String direccion,
            boolean activo, int cantidadMascotas
    ) {
        public static DuenoResponse desde(Dueno d) {
            return new DuenoResponse(d.getId(), d.getTipoDocumento(), d.getNumeroDocumento(),
                    d.getNombres(), d.getApellidos(), d.getNombreCompleto(),
                    d.getTelefono(), d.getCorreo(), d.getDireccion(), d.isActivo(),
                    (int) d.getMascotas().stream().filter(m -> m.isActivo()).count());
        }
    }

    /** Ficha completa: el cliente con sus pacientes (RF-03). */
    public record DuenoDetalleResponse(
            DuenoResponse cliente,
            List<MascotaResponse> mascotas
    ) {}

    /**
     * Edicion de los datos de contacto de un cliente.
     *
     * El correo no se incluye: es tambien el nombre de usuario con el
     * que inicia sesion, de modo que cambiarlo desde aqui dejaria la
     * cuenta inconsistente. Se trata como una operacion aparte.
     */
    public record DuenoRequest(
            @NotBlank @Size(max = 10) String tipoDocumento,
            @NotBlank @Size(max = 20) String numeroDocumento,
            @NotBlank @Size(max = 80) String nombres,
            @NotBlank @Size(max = 80) String apellidos,
            @NotBlank @Size(max = 20) String telefono,
            @Size(max = 150) String direccion
    ) {}

    /** Alta de cliente hecha por el personal, para quien llega sin cuenta. */
    public record DuenoAltaRequest(
            @NotBlank @Size(max = 10) String tipoDocumento,
            @NotBlank @Size(max = 20) String numeroDocumento,
            @NotBlank @Size(max = 80) String nombres,
            @NotBlank @Size(max = 80) String apellidos,
            @NotBlank @Size(max = 20) String telefono,
            @NotBlank @Email @Size(max = 120) String correo,
            @Size(max = 150) String direccion
    ) {}
}
