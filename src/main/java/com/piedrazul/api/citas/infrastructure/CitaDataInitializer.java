package com.piedrazul.api.citas.infrastructure;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.piedrazul.api.citas.domain.Cita;
import com.piedrazul.api.citas.domain.EstadoCita;
import com.piedrazul.api.citas.infrastructure.repository.CitaRepository;
import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.domain.User;
import com.piedrazul.api.iam.provider.PasswordHasher;
import com.piedrazul.api.iam.repository.UserRepository;

@Component
public class CitaDataInitializer implements ApplicationRunner {

    private final CitaRepository citaRepository;
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    public CitaDataInitializer(CitaRepository citaRepository,
                              UserRepository userRepository,
                              PasswordHasher passwordHasher) {
        this.citaRepository = citaRepository;
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (citaRepository.count() > 0) {
            return;
        }

        // Crear pacientes de muestra
        crearPacienteSiNoExiste("ana.ruiz@gmail.com", "Ana Ruiz", "1061000001", "3001112233");
        crearPacienteSiNoExiste("marta.gomez@gmail.com", "Marta Gómez", "1061000002", "3002223344");
        crearPacienteSiNoExiste("carlos.medina@gmail.com", "Carlos Medina", "1061000003", "3003334455");
        crearPacienteSiNoExiste("camila.rojas@gmail.com", "Camila Rojas", "1061000004", "3004445566");
        crearPacienteSiNoExiste("diego.herrera@gmail.com", "Diego Herrera", "1061000005", "3005556677");
        crearPacienteSiNoExiste("pedro.leon@gmail.com", "Pedro León", "1061000006", "3006667788");
        crearPacienteSiNoExiste("lucia.fernandez@gmail.com", "Lucía Fernández", "1061000007", "3007778899");
        crearPacienteSiNoExiste("sofia.castro@gmail.com", "Sofía Castro", "1061000008", "3008889900");
        crearPacienteSiNoExiste("isabel.vargas@gmail.com", "Isabel Vargas", "1061000009", "3009990011");
        crearPacienteSiNoExiste("santiago.morales@gmail.com", "Santiago Morales", "1061000010", "3010001122");
        crearPacienteSiNoExiste("felipe.torres@gmail.com", "Felipe Torres", "1061000011", "3011112233");
        crearPacienteSiNoExiste("elena.ramirez@gmail.com", "Elena Ramírez", "1061000012", "3012223344");

        // Fechas de muestra basadas en fechas próximas (ej. noviembre 2026 o febrero)
        LocalDate hoy = LocalDate.now();
        LocalDate d1 = hoy.plusDays(1);
        LocalDate d2 = hoy.plusDays(2);
        LocalDate d3 = hoy.plusDays(4);
        LocalDate d4 = hoy.plusDays(7);

        List<Cita> citas = List.of(
                new Cita("ana.ruiz@gmail.com", "doc-4", d1, LocalTime.of(8, 0), EstadoCita.CONFIRMADA),
                new Cita("marta.gomez@gmail.com", "doc-5", d1, LocalTime.of(8, 30), EstadoCita.CONFIRMADA),
                new Cita("carlos.medina@gmail.com", "doc-4", d1, LocalTime.of(9, 0), EstadoCita.PENDIENTE),
                new Cita("camila.rojas@gmail.com", "doc-6", d1, LocalTime.of(9, 0), EstadoCita.CONFIRMADA),
                new Cita("felipe.torres@gmail.com", "doc-1", d1, LocalTime.of(10, 0), EstadoCita.CONFIRMADA),
                new Cita("diego.herrera@gmail.com", "doc-6", d2, LocalTime.of(13, 30), EstadoCita.CONFIRMADA),
                new Cita("pedro.leon@gmail.com", "doc-5", d2, LocalTime.of(10, 0), EstadoCita.COMPLETADA),
                new Cita("lucia.fernandez@gmail.com", "doc-4", d2, LocalTime.of(11, 0), EstadoCita.CONFIRMADA),
                new Cita("sofia.castro@gmail.com", "doc-5", d2, LocalTime.of(15, 0), EstadoCita.CONFIRMADA),
                new Cita("isabel.vargas@gmail.com", "doc-7", d2, LocalTime.of(9, 30), EstadoCita.CONFIRMADA),
                new Cita("santiago.morales@gmail.com", "doc-7", d3, LocalTime.of(11, 0), EstadoCita.CANCELADA),
                new Cita("elena.ramirez@gmail.com", "doc-2", d3, LocalTime.of(14, 0), EstadoCita.CONFIRMADA),
                new Cita("ana.ruiz@gmail.com", "doc-1", d4, LocalTime.of(8, 30), EstadoCita.PENDIENTE),
                new Cita("carlos.medina@gmail.com", "doc-2", d4, LocalTime.of(11, 30), EstadoCita.CONFIRMADA),
                new Cita("camila.rojas@gmail.com", "doc-4", d4, LocalTime.of(14, 0), EstadoCita.CONFIRMADA),
                new Cita("diego.herrera@gmail.com", "doc-5", d4, LocalTime.of(16, 0), EstadoCita.CONFIRMADA)
        );

        citaRepository.saveAll(citas);
    }

    private void crearPacienteSiNoExiste(String email, String nombre, String id, String telefono) {
        if (userRepository.findByEmail(email).isEmpty()) {
            User user = new User(
                    UUID.randomUUID().toString(),
                    nombre,
                    email,
                    id,
                    telefono,
                    passwordHasher.hash("Paciente123*"),
                    RoleEnum.PATIENT
            );
            userRepository.save(user);
        }
    }
}
