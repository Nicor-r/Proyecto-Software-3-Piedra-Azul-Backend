package com.piedrazul.api.citas.dto;

import java.util.List;

public record ReporteCitasRowDTO(
        String medicoId,
        String medicoNombre,
        String tipoProfesional,
        long totalAnual,
        List<ReporteCitasMesDatoDTO> porMes
) {
}
