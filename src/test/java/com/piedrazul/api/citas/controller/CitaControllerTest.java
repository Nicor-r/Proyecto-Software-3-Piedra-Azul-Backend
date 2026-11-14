package com.piedrazul.api.citas.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.piedrazul.api.citas.dto.PacienteSugerenciaDTO;
import com.piedrazul.api.citas.exception.GlobalExceptionHandler;
import com.piedrazul.api.citas.service.CitaCsvExporter;
import com.piedrazul.api.citas.service.CitaService;
import com.piedrazul.api.citas.service.SugerenciaPacientesService;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.iam.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CitaControllerTest {

    @Mock
    private CitaService citaService;

    @Mock
    private CitaCsvExporter csvExporter;

    @Mock
    private UserRepository userRepository;

    @Mock
    private SpringDataTerapistaRepository terapistaRepository;

    @Mock
    private SugerenciaPacientesService sugerenciaPacientesService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        CitaController controller = new CitaController(
                citaService, csvExporter, userRepository, terapistaRepository, sugerenciaPacientesService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void sugerirPacientes_conPrefijoValido_retorna200ConListaSugerencias() throws Exception {
        when(sugerenciaPacientesService.sugerirPorCedula("106")).thenReturn(List.of(
                new PacienteSugerenciaDTO("1061000001", "Ana Ruiz"),
                new PacienteSugerenciaDTO("1061000002", "Marta Gómez")));

        mockMvc.perform(get("/api/citas/pacientes/sugerencias").param("cedula", "106"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].cedula").value("1061000001"))
                .andExpect(jsonPath("$[0].nombreCompleto").value("Ana Ruiz"));
    }

    @Test
    void sugerirPacientes_sinParametroCedula_retorna200ConListaVacia() throws Exception {
        when(sugerenciaPacientesService.sugerirPorCedula(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/citas/pacientes/sugerencias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
