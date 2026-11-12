package com.piedrazul.api.citas.dto;

import java.util.List;

import com.piedrazul.api.citas.domain.TipoReporteCitas;

public record ReporteCitasResponse(
        int anio,
        TipoReporteCitas tipo,
        String medicoIdFiltro,
        List<String> estadosIncluidos,
        long totalCitas,
        List<MesDTO> meses,
        List<ReporteCitasRowDTO> series
) {
    public record MesDTO(int mes, String etiqueta) {
    }
}
