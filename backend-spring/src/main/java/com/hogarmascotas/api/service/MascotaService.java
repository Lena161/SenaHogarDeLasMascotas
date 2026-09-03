package com.hogarmascotas.api.service;

import com.hogarmascotas.api.dto.MascotaDtos.*;
import com.hogarmascotas.api.entity.Mascota;
import com.hogarmascotas.api.entity.Usuario;
import com.hogarmascotas.api.repository.MascotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MascotaService {

    private final MascotaRepository mascotaRepo;

    public MascotaService(MascotaRepository mascotaRepo) {
        this.mascotaRepo = mascotaRepo;
    }

    public List<MascotaResponse> mias(Usuario solicitante) {
        return mascotaRepo.findByDuenoIdAndActivoTrue(solicitante.getDueno().getId())
                .stream().map(MascotaResponse::desde).toList();
    }

    @Transactional
    public MascotaResponse crear(MascotaRequest datos, Usuario solicitante) {
        Mascota mascota = new Mascota();
        mascota.setDueno(solicitante.getDueno()); // RN-03
        mascota.setNombre(datos.nombre().trim());
        mascota.setEspecie(datos.especie().trim().toUpperCase());
        mascota.setRaza(datos.raza());
        mascota.setFechaNacimiento(datos.fechaNacimiento());
        mascota.setSexo(datos.sexo());
        mascota.setPesoKg(datos.pesoKg());
        mascota.setTamano(datos.tamano());
        return MascotaResponse.desde(mascotaRepo.save(mascota));
    }
}
