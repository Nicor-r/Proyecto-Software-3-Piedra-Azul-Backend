package com.piedrazul.api.citas.infrastructure.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.piedrazul.api.citas.domain.Cita;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    // Buscar todas las citas de un médico en una fecha
    List<Cita> findByMedicoIdAndFecha(String medicoId, LocalDate fecha);

    List<Cita> findByMedicoIdAndFechaOrderByHoraAsc(String medicoId, LocalDate fecha);

    // Verificar si ya existe una cita en una franja específica
    boolean existsByMedicoIdAndFechaAndHora(String medicoId, LocalDate fecha, LocalTime hora);

    // Opcional: buscar todas las citas de un paciente
    List<Cita> findByPacienteId(String pacienteId); 

    List<Cita> findByMedicoIdAndFechaBetween(String medicoId, LocalDate desde, LocalDate hasta);

    List<Cita> findByMedicoIdAndEstado(String medicoId, String estado);

    List<Cita> findByPacienteIdAndEstado(String pacienteId, String estado);
}
