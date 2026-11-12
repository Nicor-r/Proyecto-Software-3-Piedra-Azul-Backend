package com.piedrazul.api.citas.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;

/**
 * Inicializador de datos históricos de prueba para el reporte gráfico de citas por mes y médico.
 *
 * <p>Convive con {@link CitaDataInitializer} usando idempotencia por tupla única
 * {@code (medicoId, fecha, hora)} en lugar de {@code count() == 0}.</p>
 *
 * <p><strong>Cómo desactivar en producción:</strong>
 * <ul>
 *   <li>Activar un perfil distinto de {@code dev}/{@code default} (ej. {@code spring.profiles.active=prod}), o</li>
 *   <li>Establecer {@code app.reportes.seed-enabled=false} en {@code application.properties} o por variable de entorno.</li>
 * </ul>
 * </p>
 */
@Component
@Order(10)
@Profile({"dev", "default"})
@ConditionalOnProperty(name = "app.reportes.seed-enabled", havingValue = "true", matchIfMissing = true)
public class ReporteDataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ReporteDataInitializer.class);
    private static final int ANIO_SEMILLA = 2025;

    private static final List<String> PACIENTES = List.of(
            "ana.ruiz@gmail.com",
            "marta.gomez@gmail.com",
            "carlos.medina@gmail.com",
            "camila.rojas@gmail.com",
            "diego.herrera@gmail.com",
            "pedro.leon@gmail.com",
            "lucia.fernandez@gmail.com",
            "sofia.castro@gmail.com",
            "isabel.vargas@gmail.com",
            "santiago.morales@gmail.com",
            "felipe.torres@gmail.com",
            "elena.ramirez@gmail.com"
    );

    private final CitaRepository citaRepository;

    public ReporteDataInitializer(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    @Override
    public void run(String... args) {
        List<Cita> candidatas = construirCitasSemilla();
        List<Cita> nuevas = new ArrayList<>();
        Set<String> clavesEnLote = new HashSet<>();

        for (Cita cita : candidatas) {
            String clave = cita.getMedicoId() + "|" + cita.getFecha() + "|" + cita.getHora();
            if (!clavesEnLote.add(clave)) {
                continue;
            }
            if (!citaRepository.existsByMedicoIdAndFechaAndHora(
                    cita.getMedicoId(), cita.getFecha(), cita.getHora())) {
                nuevas.add(cita);
            }
        }

        if (!nuevas.isEmpty()) {
            citaRepository.saveAll(nuevas);
            log.info("Sembradas {} citas históricas de prueba para reportes (año {})", nuevas.size(), ANIO_SEMILLA);
        }
    }

    private List<Cita> construirCitasSemilla() {
        List<Cita> citas = new ArrayList<>();

        // Cantidades mensuales (Ene..Dic) de citas válidas por profesional en 2025
        agregarSerieMensual(citas, "doc-4", new int[]{6, 4, 11, 7, 12, 8, 5, 10, 6, 9, 13, 4});
        agregarSerieMensual(citas, "doc-1", new int[]{3, 5, 4, 8, 6, 3, 7, 5, 4, 10, 6, 3});
        agregarSerieMensual(citas, "doc-5", new int[]{2, 0, 5, 3, 6, 4, 2, 6, 3, 5, 4, 2});
        agregarSerieMensual(citas, "doc-6", new int[]{4, 3, 2, 0, 5, 7, 3, 4, 5, 3, 6, 2});
        agregarSerieMensual(citas, "ter-001", new int[]{3, 4, 6, 3, 5, 4, 2, 5, 4, 6, 3, 2});
        agregarSerieMensual(citas, "ter-002", new int[]{2, 3, 2, 4, 3, 0, 4, 3, 2, 4, 3, 1});

        // Citas CANCELADA adicionales para verificar su exclusión del reporte
        citas.add(new Cita(PACIENTES.get(0), "doc-4", LocalDate.of(ANIO_SEMILLA, 2, 22), LocalTime.of(16, 0), EstadoCita.CANCELADA));
        citas.add(new Cita(PACIENTES.get(1), "doc-4", LocalDate.of(ANIO_SEMILLA, 6, 22), LocalTime.of(16, 30), EstadoCita.CANCELADA));
        citas.add(new Cita(PACIENTES.get(2), "doc-1", LocalDate.of(ANIO_SEMILLA, 4, 22), LocalTime.of(16, 0), EstadoCita.CANCELADA));
        citas.add(new Cita(PACIENTES.get(3), "ter-001", LocalDate.of(ANIO_SEMILLA, 9, 22), LocalTime.of(16, 0), EstadoCita.CANCELADA));

        // Caso borde 1: doc-3 está inactivo, pero con 1 cita COMPLETADA debe aparecer en el reporte
        citas.add(new Cita(PACIENTES.get(4), "doc-3", LocalDate.of(ANIO_SEMILLA, 7, 15), LocalTime.of(9, 0), EstadoCita.COMPLETADA));

        // Caso borde 2: doc-2 solo tiene citas CANCELADA (ninguna válida) → NO debe aparecer en el reporte
        citas.add(new Cita(PACIENTES.get(5), "doc-2", LocalDate.of(ANIO_SEMILLA, 3, 18), LocalTime.of(10, 0), EstadoCita.CANCELADA));
        citas.add(new Cita(PACIENTES.get(6), "doc-2", LocalDate.of(ANIO_SEMILLA, 8, 20), LocalTime.of(11, 0), EstadoCita.CANCELADA));

        return citas;
    }

    private void agregarSerieMensual(List<Cita> destino, String medicoId, int[] cantidadesPorMes) {
        for (int mes = 1; mes <= 12; mes++) {
            int cantidad = cantidadesPorMes[mes - 1];
            for (int idx = 0; idx < cantidad; idx++) {
                // Hasta 8 franjas en el día 10 y el resto en el día 11 para garantizar unicidad (medicoId, fecha, hora)
                int dia = 10 + (idx / 8);
                int franja = idx % 8;
                LocalDate fecha = LocalDate.of(ANIO_SEMILLA, mes, dia);
                LocalTime hora = LocalTime.of(8, 0).plusMinutes(franja * 30L);
                String paciente = PACIENTES.get((mes + idx) % PACIENTES.size());
                EstadoCita estado = seleccionarEstadoValido(mes, idx);
                destino.add(new Cita(paciente, medicoId, fecha, hora, estado));
            }
        }
    }

    private EstadoCita seleccionarEstadoValido(int mes, int idx) {
        int mod = (mes + idx) % 6;
        if (mod == 0) {
            return EstadoCita.PENDIENTE;
        }
        if (mod <= 2) {
            return EstadoCita.CONFIRMADA;
        }
        return EstadoCita.COMPLETADA;
    }
}
