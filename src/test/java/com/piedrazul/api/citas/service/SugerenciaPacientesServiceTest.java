package com.piedrazul.api.citas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.citas.dto.PacienteSugerenciaDTO;
import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class SugerenciaPacientesServiceTest {

    @Mock
    private UserRepository userRepository;

    private SugerenciaPacientesService service;

    @BeforeEach
    void setUp() {
        service = new SugerenciaPacientesService(userRepository);
    }

    @Test
    void sugerirPorCedula_conTresDigitos_retornaCedulaYNombreDeLosPacientes() {
        User ana = new User("pac-1", "Ana Ruiz", "ana.ruiz@gmail.com", "1061000001", "3001112233", "hash", RoleEnum.PATIENT);
        User marta = new User("pac-2", "Marta Gomez", "marta.gomez@gmail.com", "1061000002", "3002223344", "hash", RoleEnum.PATIENT);
        when(userRepository.findTop10ByRoleAndNumeroIdentificacionStartingWith(RoleEnum.PATIENT, "106"))
                .thenReturn(List.of(ana, marta));

        List<PacienteSugerenciaDTO> sugerencias = service.sugerirPorCedula("106");

        assertThat(sugerencias).containsExactly(
                new PacienteSugerenciaDTO("1061000001", "Ana Ruiz"),
                new PacienteSugerenciaDTO("1061000002", "Marta Gomez"));
    }

    @Test
    void sugerirPorCedula_sinCoincidencias_retornaListaVacia() {
        when(userRepository.findTop10ByRoleAndNumeroIdentificacionStartingWith(RoleEnum.PATIENT, "999"))
                .thenReturn(List.of());

        assertThat(service.sugerirPorCedula("999")).isEmpty();
    }

    @Test
    void sugerirPorCedula_conMenosDeTresDigitos_noConsultaYRetornaVacia() {
        assertThat(service.sugerirPorCedula("10")).isEmpty();

        verify(userRepository, never()).findTop10ByRoleAndNumeroIdentificacionStartingWith(any(), any());
    }

    @Test
    void sugerirPorCedula_conLetras_noConsultaYRetornaVacia() {
        assertThat(service.sugerirPorCedula("10a")).isEmpty();

        verify(userRepository, never()).findTop10ByRoleAndNumeroIdentificacionStartingWith(any(), any());
    }

    @Test
    void sugerirPorCedula_nulo_noConsultaYRetornaVacia() {
        assertThat(service.sugerirPorCedula(null)).isEmpty();

        verify(userRepository, never()).findTop10ByRoleAndNumeroIdentificacionStartingWith(any(), any());
    }

    @Test
    void sugerirPorCedula_conEspaciosAlrededor_buscaConElPrefijoLimpio() {
        when(userRepository.findTop10ByRoleAndNumeroIdentificacionStartingWith(RoleEnum.PATIENT, "106"))
                .thenReturn(List.of());

        service.sugerirPorCedula("  106 ");

        verify(userRepository).findTop10ByRoleAndNumeroIdentificacionStartingWith(RoleEnum.PATIENT, "106");
    }
}