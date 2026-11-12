package com.piedrazul.api.citas.service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.domain.TipoReporteCitas;
import com.piedrazul.api.citas.dto.ReporteCitasMesDatoDTO;
import com.piedrazul.api.citas.dto.ReporteCitasResponse;
import com.piedrazul.api.citas.dto.ReporteCitasResponse.MesDTO;
import com.piedrazul.api.citas.dto.ReporteCitasRowDTO;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.iam.infrastructure.entity.TerapistaJpaEntity;
import com.piedrazul.api.iam.infrastructure.repository.SpringDataTerapistaRepository;
import com.piedrazul.api.scheduling.domain.Doctor;
import com.piedrazul.api.scheduling.repository.DoctorRepository;

@Service
public class ReporteCitasService {

    private static final int ANIO_MINIMO = 2000;
    private static final int ANIO_MAXIMO = 2100;

    private static final List<MesDTO> MESES_DEL_ANIO = List.of(
            new MesDTO(1, "Ene"),
            new MesDTO(2, "Feb"),
            new MesDTO(3, "Mar"),
            new MesDTO(4, "Abr"),
            new MesDTO(5, "May"),
            new MesDTO(6, "Jun"),
            new MesDTO(7, "Jul"),
            new MesDTO(8, "Ago"),
            new MesDTO(9, "Sep"),
            new MesDTO(10, "Oct"),
            new MesDTO(11, "Nov"),
            new MesDTO(12, "Dic")
    );

    private static final List<String> ESTADOS_INCLUIDOS = Arrays.stream(EstadoCita.values())
            .filter(estado -> estado != EstadoCita.CANCELADA)
            .map(Enum::name)
            .toList();

    private final CitaRepository citaRepository;
    private final DoctorRepository doctorRepository;
    private final SpringDataTerapistaRepository terapistaRepository;

    public ReporteCitasService(CitaRepository citaRepository,
                               DoctorRepository doctorRepository,
                               SpringDataTerapistaRepository terapistaRepository) {
        this.citaRepository = citaRepository;
        this.doctorRepository = doctorRepository;
        this.terapistaRepository = terapistaRepository;
    }

    @Transactional(readOnly = true)
    public ReporteCitasResponse generarReporteCitasPorMes(Integer anio, String medicoId, String tipoTexto) {
        int anioValidado = validarAnio(anio);
        TipoReporteCitas tipo = parsearTipo(tipoTexto);
        String medicoIdFiltro = normalizarMedicoId(medicoId);

        if (medicoIdFiltro != null) {
            validarMedicoExiste(medicoIdFiltro);
        }

        LocalDate inicioAnio = LocalDate.of(anioValidado, 1, 1);
        LocalDate finAnio = LocalDate.of(anioValidado, 12, 31);

        List<Cita> citas = (medicoIdFiltro != null)
                ? citaRepository.findByMedicoIdAndFechaBetweenAndEstadoNot(
                        medicoIdFiltro, inicioAnio, finAnio, EstadoCita.CANCELADA)
                : citaRepository.findByFechaBetweenAndEstadoNot(
                        inicioAnio, finAnio, EstadoCita.CANCELADA);

        Map<String, Map<Integer, Long>> conteoPorMedicoYMes = agregarPorMedicoYMes(citas);

        if (conteoPorMedicoYMes.isEmpty()) {
            return new ReporteCitasResponse(
                    anioValidado,
                    tipo,
                    medicoIdFiltro,
                    ESTADOS_INCLUIDOS,
                    0L,
                    MESES_DEL_ANIO,
                    List.of()
            );
        }

        Map<String, ProfesionalInfo> catalogoProfesionales = construirCatalogoProfesionales();

        List<ReporteCitasRowDTO> series = conteoPorMedicoYMes.entrySet().stream()
                .map(entry -> construirSerie(entry.getKey(), entry.getValue(), catalogoProfesionales))
                .filter(row -> row.totalAnual() > 0)
                .sorted(Comparator.comparing(ReporteCitasRowDTO::medicoNombre, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ReporteCitasRowDTO::medicoId))
                .toList();

        long totalCitas = series.stream()
                .mapToLong(ReporteCitasRowDTO::totalAnual)
                .sum();

        return new ReporteCitasResponse(
                anioValidado,
                tipo,
                medicoIdFiltro,
                ESTADOS_INCLUIDOS,
                totalCitas,
                MESES_DEL_ANIO,
                series
        );
    }

    private int validarAnio(Integer anio) {
        if (anio == null) {
            throw new IllegalArgumentException("El año es obligatorio");
        }
        if (anio < ANIO_MINIMO || anio > ANIO_MAXIMO) {
            throw new IllegalArgumentException(
                    "El año debe estar entre " + ANIO_MINIMO + " y " + ANIO_MAXIMO);
        }
        return anio;
    }

    private TipoReporteCitas parsearTipo(String tipoTexto) {
        if (tipoTexto == null || tipoTexto.isBlank()) {
            return TipoReporteCitas.POR_MES;
        }
        try {
            return TipoReporteCitas.valueOf(tipoTexto.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Tipo de reporte inválido: " + tipoTexto);
        }
    }

    private String normalizarMedicoId(String medicoId) {
        if (medicoId == null || medicoId.isBlank()) {
            return null;
        }
        return medicoId.trim();
    }

    private void validarMedicoExiste(String medicoId) {
        boolean existe = doctorRepository.existsById(medicoId) || terapistaRepository.existsById(medicoId);
        if (!existe) {
            throw new IllegalArgumentException("El médico/terapista seleccionado no existe");
        }
    }

    private Map<String, Map<Integer, Long>> agregarPorMedicoYMes(List<Cita> citas) {
        Map<String, Map<Integer, Long>> resultado = new HashMap<>();
        for (Cita cita : citas) {
            if (cita.getEstado() == EstadoCita.CANCELADA
                    || cita.getFecha() == null
                    || cita.getMedicoId() == null
                    || cita.getMedicoId().isBlank()) {
                continue;
            }
            int mes = cita.getFecha().getMonthValue();
            resultado
                    .computeIfAbsent(cita.getMedicoId(), k -> new HashMap<>())
                    .merge(mes, 1L, Long::sum);
        }
        return resultado;
    }

    private Map<String, ProfesionalInfo> construirCatalogoProfesionales() {
        Map<String, ProfesionalInfo> catalogo = new HashMap<>();
        for (Doctor doctor : doctorRepository.findAll()) {
            catalogo.put(doctor.getId(), new ProfesionalInfo(doctor.getNombre(), "MEDICO"));
        }
        for (TerapistaJpaEntity terapista : terapistaRepository.findAll()) {
            String nombre = formatearNombreTerapista(terapista);
            catalogo.putIfAbsent(terapista.getId(), new ProfesionalInfo(nombre, "TERAPISTA"));
        }
        return catalogo;
    }

    private String formatearNombreTerapista(TerapistaJpaEntity terapista) {
        String especialidad = terapista.getEspecialidad();
        if (especialidad != null && !especialidad.isBlank()) {
            return terapista.getNombre() + " - " + especialidad.trim();
        }
        return terapista.getNombre();
    }

    private ReporteCitasRowDTO construirSerie(String medicoId,
                                            Map<Integer, Long> conteoPorMes,
                                            Map<String, ProfesionalInfo> catalogo) {
        ProfesionalInfo info = catalogo.getOrDefault(medicoId, new ProfesionalInfo(medicoId, "DESCONOCIDO"));

        List<ReporteCitasMesDatoDTO> porMes = IntStream.rangeClosed(1, 12)
                .mapToObj(mes -> new ReporteCitasMesDatoDTO(mes, conteoPorMes.getOrDefault(mes, 0L)))
                .toList();

        long totalAnual = porMes.stream()
                .mapToLong(ReporteCitasMesDatoDTO::cantidad)
                .sum();

        return new ReporteCitasRowDTO(
                medicoId,
                info.nombre(),
                info.tipoProfesional(),
                totalAnual,
                porMes
        );
    }

    private record ProfesionalInfo(String nombre, String tipoProfesional) {
    }
}
