package com.piedrazul.api.citas.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.piedrazul.api.citas.dto.PacienteSugerenciaDTO;
import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.repository.UserRepository;

/**
 * Autocompletado de cedula de pacientes (HE-05, HU-5.1).
 *
 * Regla: con 3 a 10 digitos busca hasta 10 pacientes cuya cedula empiece por
 * esos digitos. Con menos de 3 digitos (o con letras) NO consulta la base de
 * datos y devuelve una lista vacia.
 */
@Service
public class SugerenciaPacientesService {

    // Entre 3 y 10 digitos: menos de 3 no se consulta (escenario 4); mas de 10 no puede ser cedula.
    private static final String PATRON_PREFIJO = "\\d{3,10}";

    private final UserRepository userRepository;

    public SugerenciaPacientesService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<PacienteSugerenciaDTO> sugerirPorCedula(String prefijo) {
        if (prefijo == null) {
            return List.of();
        }

        String limpio = prefijo.trim();
        if (!limpio.matches(PATRON_PREFIJO)) {
            return List.of();
        }

        return userRepository.findTop10ByRoleAndNumeroIdentificacionStartingWith(RoleEnum.PATIENT, limpio)
                .stream()
                .map(usuario -> new PacienteSugerenciaDTO(usuario.getNumeroIdentificacion(),
                        usuario.getNombreCompleto()))
                .toList();
    }
}