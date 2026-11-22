package com.piedrazul.api.citas.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.dto.CitaDTO;
import com.piedrazul.api.citas.exception.CancelacionNoPermitidaException;
import com.piedrazul.api.citas.exception.GlobalExceptionHandler;
import com.piedrazul.api.citas.security.AuthenticatedUser;
import com.piedrazul.api.citas.service.CitaCsvExporter;
import com.piedrazul.api.citas.service.CitaService;
import com.piedrazul.api.citas.service.SugerenciaPacientesService;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.iam.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CitaControllerCancelarTest {

    private static final String PACIENTE = "paciente@piedrazul.com";

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
        HandlerMethodArgumentResolver usuarioFijo = new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(AuthenticatedUser.class);
            }

            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mav,
                    NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                return PACIENTE;
            }
        };
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(usuarioFijo)
                .build();
    }

    private static String json(String motivo) {
        return "{\"motivo\":" + (motivo == null ? "null" : "\"" + motivo + "\"") + "}";
    }

    @Test
    void cancelar_ok_retorna200ConCitaCancelada() throws Exception {
        CitaDTO dto = new CitaDTO(7L, PACIENTE, "doc-1", LocalDate.of(2026, 12, 1), LocalTime.of(9, 0),
                EstadoCita.CANCELADA);
        dto.setMotivoCancelacion("Viaje");
        dto.setFechaCancelacion(LocalDateTime.of(2026, 11, 22, 10, 0));
        when(citaService.cancelarCita(7L, PACIENTE, "Viaje")).thenReturn(dto);

        mockMvc.perform(put("/api/citas/7/cancelar").contentType(MediaType.APPLICATION_JSON).content(json("Viaje")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.estado").value("CANCELADA"))
                .andExpect(jsonPath("$.motivoCancelacion").value("Viaje"))
                .andExpect(jsonPath("$.fechaCancelacion").exists());
    }

    @Test
    void cancelar_sinMotivo_retorna400SinLlamarAlServicio() throws Exception {
        mockMvc.perform(put("/api/citas/7/cancelar").contentType(MediaType.APPLICATION_JSON).content(json("  ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("justificación")));

        verify(citaService, never()).cancelarCita(any(), any(), any());
    }

    @Test
    void cancelar_motivoNulo_retorna400() throws Exception {
        mockMvc.perform(put("/api/citas/7/cancelar").contentType(MediaType.APPLICATION_JSON).content(json(null)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelar_motivoMuyLargo_retorna400() throws Exception {
        mockMvc.perform(put("/api/citas/7/cancelar").contentType(MediaType.APPLICATION_JSON)
                .content(json("x".repeat(501))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void cancelar_reglaDeNegocioIncumplida_retorna400ConMensaje() throws Exception {
        when(citaService.cancelarCita(eq(7L), eq(PACIENTE), any()))
                .thenThrow(new CancelacionNoPermitidaException("Solo se puede cancelar una cita con al menos 2 días de anticipación"));

        mockMvc.perform(put("/api/citas/7/cancelar").contentType(MediaType.APPLICATION_JSON).content(json("Imprevisto")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("2 días")));
    }

    @Test
    void cancelar_citaDeOtroPaciente_retorna403() throws Exception {
        when(citaService.cancelarCita(eq(7L), eq(PACIENTE), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "La cita no pertenece al paciente autenticado"));

        mockMvc.perform(put("/api/citas/7/cancelar").contentType(MediaType.APPLICATION_JSON).content(json("Motivo")))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelar_citaInexistente_retorna404() throws Exception {
        when(citaService.cancelarCita(eq(99L), eq(PACIENTE), any()))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe la cita"));

        mockMvc.perform(put("/api/citas/99/cancelar").contentType(MediaType.APPLICATION_JSON).content(json("Motivo")))
                .andExpect(status().isNotFound());
    }

    @Test
    void cancelar_idNoNumerico_retorna400() throws Exception {
        mockMvc.perform(put("/api/citas/abc/cancelar").contentType(MediaType.APPLICATION_JSON).content(json("Motivo")))
                .andExpect(status().isBadRequest());
    }
}
