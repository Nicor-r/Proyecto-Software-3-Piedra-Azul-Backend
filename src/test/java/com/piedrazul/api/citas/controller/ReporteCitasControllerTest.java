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

import com.piedrazul.api.citas.domain.TipoReporteCitas;
import com.piedrazul.api.citas.dto.ReporteCitasMesDatoDTO;
import com.piedrazul.api.citas.dto.ReporteCitasResponse;
import com.piedrazul.api.citas.dto.ReporteCitasResponse.MesDTO;
import com.piedrazul.api.citas.dto.ReporteCitasRowDTO;
import com.piedrazul.api.citas.exception.GlobalExceptionHandler;
import com.piedrazul.api.citas.service.ReporteCitasService;

@ExtendWith(MockitoExtension.class)
class ReporteCitasControllerTest {

    @Mock
    private ReporteCitasService reporteCitasService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ReporteCitasController controller = new ReporteCitasController(reporteCitasService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void obtenerCitasPorMes_requestValido_retorna200ConEstructuraCompleta() throws Exception {
        List<MesDTO> meses = List.of(
                new MesDTO(1, "Ene"), new MesDTO(2, "Feb"), new MesDTO(3, "Mar"),
                new MesDTO(4, "Abr"), new MesDTO(5, "May"), new MesDTO(6, "Jun"),
                new MesDTO(7, "Jul"), new MesDTO(8, "Ago"), new MesDTO(9, "Sep"),
                new MesDTO(10, "Oct"), new MesDTO(11, "Nov"), new MesDTO(12, "Dic")
        );
        List<ReporteCitasMesDatoDTO> porMes = List.of(
                new ReporteCitasMesDatoDTO(1, 6L), new ReporteCitasMesDatoDTO(2, 0L),
                new ReporteCitasMesDatoDTO(3, 0L), new ReporteCitasMesDatoDTO(4, 0L),
                new ReporteCitasMesDatoDTO(5, 0L), new ReporteCitasMesDatoDTO(6, 0L),
                new ReporteCitasMesDatoDTO(7, 0L), new ReporteCitasMesDatoDTO(8, 0L),
                new ReporteCitasMesDatoDTO(9, 0L), new ReporteCitasMesDatoDTO(10, 0L),
                new ReporteCitasMesDatoDTO(11, 0L), new ReporteCitasMesDatoDTO(12, 0L)
        );
        ReporteCitasResponse response = new ReporteCitasResponse(
                2025,
                TipoReporteCitas.POR_MES,
                null,
                List.of("PENDIENTE", "CONFIRMADA", "COMPLETADA"),
                6L,
                meses,
                List.of(new ReporteCitasRowDTO(
                        "doc-4",
                        "Dra. Ibis González - Cardiología",
                        "MEDICO",
                        6L,
                        porMes
                ))
        );

        when(reporteCitasService.generarReporteCitasPorMes(2025, null, "POR_MES"))
                .thenReturn(response);

        mockMvc.perform(get("/api/reportes/citas-por-mes")
                        .param("anio", "2025"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.anio").value(2025))
                .andExpect(jsonPath("$.tipo").value("POR_MES"))
                .andExpect(jsonPath("$.totalCitas").value(6))
                .andExpect(jsonPath("$.meses.length()").value(12))
                .andExpect(jsonPath("$.series.length()").value(1))
                .andExpect(jsonPath("$.series[0].medicoId").value("doc-4"))
                .andExpect(jsonPath("$.series[0].porMes.length()").value(12));
    }

    @Test
    void obtenerCitasPorMes_sinAnio_retorna400BadRequest() throws Exception {
        mockMvc.perform(get("/api/reportes/citas-por-mes"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Falta el parámetro obligatorio: anio"));
    }

    @Test
    void obtenerCitasPorMes_anioNoNumerico_retorna400BadRequest() throws Exception {
        mockMvc.perform(get("/api/reportes/citas-por-mes")
                        .param("anio", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("El parámetro 'anio' tiene un formato inválido"));
    }
}
