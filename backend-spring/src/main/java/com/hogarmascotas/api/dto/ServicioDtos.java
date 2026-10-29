package com.hogarmascotas.api.dto;

import com.hogarmascotas.api.entity.Servicio;
import com.hogarmascotas.api.entity.ServicioSpa;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class ServicioDtos {

    public record ServicioResponse(
            Long id, String nombre, String tipo, String descripcion,
            int duracionMinutos, BigDecimal precioBase, BigDecimal recargoPorTamano,
            boolean activo
    ) {
        public static ServicioResponse desde(Servicio s) {
            BigDecimal recargo = (s instanceof ServicioSpa spa) ? spa.getRecargoPorTamano() : null;
            String tipo = (s instanceof ServicioSpa) ? "SPA" : "CONSULTA";
            return new ServicioResponse(s.getId(), s.getNombre(), tipo,
                    s.getDescripcion(), s.getDuracionMinutos(), s.getPrecioBase(),
                    recargo, s.isActivo());
        }
    }

    /**
     * Alta y edicion de servicios (RF-04).
     *
     * El tipo determina que subclase se instancia y, por lo tanto, como
     * se calculara el precio y quien podra atender el servicio. Por eso
     * solo puede fijarse al crear: cambiarlo despues convertiria una
     * consulta en un bano, con citas historicas ya asociadas.
     */
    public record ServicioRequest(
            @NotBlank @Size(max = 80) String nombre,
            @NotNull Tipo tipo,
            @Size(max = 255) String descripcion,
            @Min(value = 5, message = "La duracion minima es de 5 minutos")
            @Max(value = 480, message = "La duracion maxima es de 8 horas")
            int duracionMinutos,
            @NotNull @DecimalMin(value = "0.0", inclusive = false,
                    message = "El precio debe ser mayor que cero") BigDecimal precioBase,
            @DecimalMin(value = "0.0", message = "El recargo no puede ser negativo")
            BigDecimal recargoPorTamano
    ) {
        public enum Tipo { CONSULTA, SPA }
    }
}
