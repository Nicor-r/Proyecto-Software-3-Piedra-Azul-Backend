package com.piedrazul.api.citas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.domain.TipoReporteCitas;
import com.piedrazul.api.citas.dto.ReporteCitasMesDatoDTO;
import com.piedrazul.api.citas.dto.ReporteCitasResponse;
import com.piedrazul.api.citas.dto.ReporteCitasRowDTO;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@ExtendWith(MockitoExtension.class)
class ReporteCitasServiceTest {

    @Mock
    private CitaRepository citaRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private SpringDataTerapistaRepository terapistaRepository;

    private ReporteCitasService service;

    @BeforeEach
    void setUp() {
        service = new ReporteCitasService(citaRepository, doctorRepository, terapistaRepository);
    }

    @Test
    void generarReporteCitasPorMes_conDoctoresYTerapistas_retorna12MesesYExcluyeCanceladas() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 12, 31);

        List<Cita> citas = List.of(
                new Cita("p1@gmail.com", "doc-4", LocalDate.of(2025, 1, 10), LocalTime.of(8, 0), EstadoCita.CONFIRMADA),
                new Cita("p2@gmail.com", "doc-4", LocalDate.of(2025, 1, 15), LocalTime.of(9, 0), EstadoCita.COMPLETADA),
                new Cita("p3@gmail.com", "doc-4", LocalDate.of(2025, 3, 5), LocalTime.of(10, 0), EstadoCita.PENDIENTE),
                new Cita("p4@gmail.com", "doc-4", LocalDate.of(2025, 3, 6), LocalTime.of(11, 0), EstadoCita.CANCELADA),
                new Cita("p5@gmail.com", "ter-001", LocalDate.of(2025, 2, 20), LocalTime.of(14, 0), EstadoCita.CONFIRMADA)
        );

        when(citaRepository.findByFechaBetweenAndEstadoNot(inicio, fin, EstadoCita.CANCELADA))
                .thenReturn(citas);
        when(doctorRepository.findAll()).thenReturn(List.of(
                new Doctor("doc-4", "Dra. Ibis González - Cardiología", true)
        ));
        when(terapistaRepository.findAll()).thenReturn(List.of(
                new TerapistaJpaEntity("ter-001", "u-ter-1", "Dra. Ana Terapista", "Terapia Física")
        ));

        ReporteCitasResponse response = service.generarReporteCitasPorMes(2025, null, "POR_MES");

        assertThat(response.anio()).isEqualTo(2025);
        assertThat(response.tipo()).isEqualTo(TipoReporteCitas.POR_MES);
        assertThat(response.medicoIdFiltro()).isNull();
        assertThat(response.estadosIncluidos()).containsExactly("PENDIENTE", "CONFIRMADA", "COMPLETADA");
        assertThat(response.totalCitas()).isEqualTo(4L);
        assertThat(response.meses()).hasSize(12);
        assertThat(response.meses().get(0).etiqueta()).isEqualTo("Ene");
        assertThat(response.meses().get(11).etiqueta()).isEqualTo("Dic");

        assertThat(response.series()).hasSize(2);

        ReporteCitasRowDTO serieTerapista = response.series().get(0);
        assertThat(serieTerapista.medicoId()).isEqualTo("ter-001");
        assertThat(serieTerapista.medicoNombre()).isEqualTo("Dra. Ana Terapista - Terapia Física");
        assertThat(serieTerapista.tipoProfesional()).isEqualTo("TERAPISTA");
        assertThat(serieTerapista.totalAnual()).isEqualTo(1L);
        assertThat(serieTerapista.porMes()).hasSize(12);
        assertThat(serieTerapista.porMes()).extracting(ReporteCitasMesDatoDTO::cantidad)
                .containsExactly(0L, 1L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L);

        ReporteCitasRowDTO serieDoctor = response.series().get(1);
        assertThat(serieDoctor.medicoId()).isEqualTo("doc-4");
        assertThat(serieDoctor.medicoNombre()).isEqualTo("Dra. Ibis González - Cardiología");
        assertThat(serieDoctor.tipoProfesional()).isEqualTo("MEDICO");
        assertThat(serieDoctor.totalAnual()).isEqualTo(3L);
        assertThat(serieDoctor.porMes()).hasSize(12);
        assertThat(serieDoctor.porMes().get(0).cantidad()).isEqualTo(2L);
        assertThat(serieDoctor.porMes().get(1).cantidad()).isEqualTo(0L);
        assertThat(serieDoctor.porMes().get(2).cantidad()).isEqualTo(1L);
    }

    @Test
    void generarReporteCitasPorMes_medicoConCeroCitasOSoloCanceladas_noApareceEnSeries() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 12, 31);

        List<Cita> citas = List.of(
                new Cita("p1@gmail.com", "doc-1", LocalDate.of(2025, 5, 10), LocalTime.of(9, 0), EstadoCita.COMPLETADA),
                new Cita("p2@gmail.com", "doc-2", LocalDate.of(2025, 5, 11), LocalTime.of(10, 0), EstadoCita.CANCELADA)
        );

        when(citaRepository.findByFechaBetweenAndEstadoNot(inicio, fin, EstadoCita.CANCELADA))
                .thenReturn(citas);
        when(doctorRepository.findAll()).thenReturn(List.of(
                new Doctor("doc-1", "Dra. Ana Martinez", true),
                new Doctor("doc-2", "Dr. Carlos Ruiz", true),
                new Doctor("doc-5", "Dra. Clara Inés", true)
        ));
        when(terapistaRepository.findAll()).thenReturn(List.of());

        ReporteCitasResponse response = service.generarReporteCitasPorMes(2025, null, null);

        assertThat(response.totalCitas()).isEqualTo(1L);
        assertThat(response.series())
                .extracting(ReporteCitasRowDTO::medicoId)
                .containsExactly("doc-1");
    }

    @Test
    void generarReporteCitasPorMes_medicoInactivoConCitaValida_siApareceEnSeries() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 12, 31);

        List<Cita> citas = List.of(
                new Cita("p1@gmail.com", "doc-3", LocalDate.of(2025, 6, 12), LocalTime.of(10, 0), EstadoCita.COMPLETADA)
        );

        when(citaRepository.findByFechaBetweenAndEstadoNot(inicio, fin, EstadoCita.CANCELADA))
                .thenReturn(citas);
        when(doctorRepository.findAll()).thenReturn(List.of(
                new Doctor("doc-3", "Dra. Laura Gomez - Odontologia (inactiva)", false)
        ));
        when(terapistaRepository.findAll()).thenReturn(List.of());

        ReporteCitasResponse response = service.generarReporteCitasPorMes(2025, null, "POR_MES");

        assertThat(response.totalCitas()).isEqualTo(1L);
        assertThat(response.series()).hasSize(1);
        assertThat(response.series().get(0).medicoId()).isEqualTo("doc-3");
        assertThat(response.series().get(0).totalAnual()).isEqualTo(1L);
    }

    @Test
    void generarReporteCitasPorMes_anioSinCitasValidas_retornaSeriesVaciasY12Meses() {
        when(citaRepository.findByFechaBetweenAndEstadoNot(any(), any(), eq(EstadoCita.CANCELADA)))
                .thenReturn(List.of());

        ReporteCitasResponse response = service.generarReporteCitasPorMes(2027, null, "POR_MES");

        assertThat(response.anio()).isEqualTo(2027);
        assertThat(response.totalCitas()).isEqualTo(0L);
        assertThat(response.series()).isEmpty();
        assertThat(response.meses()).hasSize(12);
    }

    @Test
    void generarReporteCitasPorMes_conFiltroMedicoIdConCitas_retornaSoloEseMedico() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 12, 31);

        when(doctorRepository.existsById("doc-4")).thenReturn(true);
        when(citaRepository.findByMedicoIdAndFechaBetweenAndEstadoNot("doc-4", inicio, fin, EstadoCita.CANCELADA))
                .thenReturn(List.of(
                        new Cita("p1@gmail.com", "doc-4", LocalDate.of(2025, 4, 3), LocalTime.of(8, 30), EstadoCita.CONFIRMADA)
                ));
        when(doctorRepository.findAll()).thenReturn(List.of(
                new Doctor("doc-4", "Dra. Ibis González - Cardiología", true)
        ));
        when(terapistaRepository.findAll()).thenReturn(List.of());

        ReporteCitasResponse response = service.generarReporteCitasPorMes(2025, "doc-4", "POR_MES");

        assertThat(response.medicoIdFiltro()).isEqualTo("doc-4");
        assertThat(response.totalCitas()).isEqualTo(1L);
        assertThat(response.series()).hasSize(1);
        assertThat(response.series().get(0).medicoId()).isEqualTo("doc-4");
        assertThat(response.series().get(0).porMes().get(3).cantidad()).isEqualTo(1L);
        verify(citaRepository).findByMedicoIdAndFechaBetweenAndEstadoNot("doc-4", inicio, fin, EstadoCita.CANCELADA);
    }

    @Test
    void generarReporteCitasPorMes_conFiltroMedicoIdSinCitas_retornaSeriesVacias() {
        LocalDate inicio = LocalDate.of(2025, 1, 1);
        LocalDate fin = LocalDate.of(2025, 12, 31);

        when(doctorRepository.existsById("ter-001")).thenReturn(false);
        when(terapistaRepository.existsById("ter-001")).thenReturn(true);
        when(citaRepository.findByMedicoIdAndFechaBetweenAndEstadoNot("ter-001", inicio, fin, EstadoCita.CANCELADA))
                .thenReturn(List.of());

        ReporteCitasResponse response = service.generarReporteCitasPorMes(2025, "ter-001", "POR_MES");

        assertThat(response.medicoIdFiltro()).isEqualTo("ter-001");
        assertThat(response.totalCitas()).isEqualTo(0L);
        assertThat(response.series()).isEmpty();
        assertThat(response.meses()).hasSize(12);
    }

    @Test
    void generarReporteCitasPorMes_parametrosInvalidos_lanzaIllegalArgumentException() {
        assertThatThrownBy(() -> service.generarReporteCitasPorMes(null, null, "POR_MES"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El año es obligatorio");

        assertThatThrownBy(() -> service.generarReporteCitasPorMes(1999, null, "POR_MES"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El año debe estar entre 2000 y 2100");

        assertThatThrownBy(() -> service.generarReporteCitasPorMes(2101, null, "POR_MES"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El año debe estar entre 2000 y 2100");

        assertThatThrownBy(() -> service.generarReporteCitasPorMes(2025, null, "POR_PACIENTE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo de reporte inválido: POR_PACIENTE");

        when(doctorRepository.existsById("no-existe")).thenReturn(false);
        when(terapistaRepository.existsById("no-existe")).thenReturn(false);

        assertThatThrownBy(() -> service.generarReporteCitasPorMes(2025, "no-existe", "POR_MES"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El médico/terapista seleccionado no existe");
    }
}
