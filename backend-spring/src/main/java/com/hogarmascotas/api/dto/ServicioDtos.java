package com.hogarmascotas.api.dto;

import com.hogarmascotas.api.entity.Servicio;
import com.hogarmascotas.api.entity.ServicioSpa;

import java.math.BigDecimal;

public class ServicioDtos {

    public record ServicioResponse(
            Long id, String nombre, String tipo, String descripcion,
            int duracionMinutos, BigDecimal precioBase, BigDecimal recargoPorTamano
    ) {
        public static ServicioResponse desde(Servicio s) {
            BigDecimal recargo = (s instanceof ServicioSpa spa) ? spa.getRecargoPorTamano() : null;
            String tipo = (s instanceof ServicioSpa) ? "SPA" : "CONSULTA";
            return new ServicioResponse(s.getId(), s.getNombre(), tipo,
                    s.getDescripcion(), s.getDuracionMinutos(), s.getPrecioBase(), recargo);
        }
    }
}
