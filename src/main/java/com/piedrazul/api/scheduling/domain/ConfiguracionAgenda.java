package com.piedrazul.api.scheduling.domain;

import java.util.Objects;

/**
 * @file ConfiguracionAgenda.java
 * @brief Entidad de dominio que agrupa los 4 Value Objects de configuración
 *        para un médico o terapista específico.
 *
 * @details
 * Esta clase representa la configuración completa de agenda de un doctor,
 * compuesta por los siguientes Value Objects:
 * - {@link VentanaAgendamiento}: ventana temporal de agendamiento.
 * - {@link DiasAtencion}: días en los que el doctor atiende.
 * - {@link FranjaHoraria}: franja horaria de atención.
 * - {@link IntervaloCitas}: intervalo entre citas.
 *
 * La igualdad entre instancias se basa únicamente en el campo {@code id}.
 */
public class ConfiguracionAgenda {


    private String id;
    private String doctorId;
    private VentanaAgendamiento ventanaAgendamiento;
    private DiasAtencion diasAtencion;
    private FranjaHoraria franjaHoraria;
    private IntervaloCitas intervaloCitas;

    public ConfiguracionAgenda() {
    }

    /**
     * @brief Constructor completo de la configuración de agenda.
     *
     * @param id                  Identificador único de la configuración.
     * @param doctorId            Identificador del doctor.
     * @param ventanaAgendamiento Value Object de ventana de agendamiento.
     * @param diasAtencion        Value Object de días de atención.
     * @param franjaHoraria       Value Object de franja horaria.
     * @param intervaloCitas      Value Object de intervalo entre citas.
     */
    public ConfiguracionAgenda(String id, String doctorId, VentanaAgendamiento ventanaAgendamiento,
            DiasAtencion diasAtencion, FranjaHoraria franjaHoraria,
            IntervaloCitas intervaloCitas) {
        this.id = id;
        this.doctorId = doctorId;
        this.ventanaAgendamiento = ventanaAgendamiento;
        this.diasAtencion = diasAtencion;
        this.franjaHoraria = franjaHoraria;
        this.intervaloCitas = intervaloCitas;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public VentanaAgendamiento getVentanaAgendamiento() {
        return ventanaAgendamiento;
    }

    public void setVentanaAgendamiento(VentanaAgendamiento ventanaAgendamiento) {
        this.ventanaAgendamiento = ventanaAgendamiento;
    }

    public DiasAtencion getDiasAtencion() {
        return diasAtencion;
    }

    public void setDiasAtencion(DiasAtencion diasAtencion) {
        this.diasAtencion = diasAtencion;
    }

    public FranjaHoraria getFranjaHoraria() {
        return franjaHoraria;
    }

    public void setFranjaHoraria(FranjaHoraria franjaHoraria) {
        this.franjaHoraria = franjaHoraria;
    }

    public IntervaloCitas getIntervaloCitas() {
        return intervaloCitas;
    }

    public void setIntervaloCitas(IntervaloCitas intervaloCitas) {
        this.intervaloCitas = intervaloCitas;
    }

    /**
     * @brief Compara esta configuración con otro objeto.
     *
     * @details
     * Dos instancias de {@code ConfiguracionAgenda} se consideran iguales si
     * tienen el mismo {@code id}.
     *
     * @param o Objeto a comparar.
     * @return {@code true} si son iguales; {@code false} en caso contrario.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ConfiguracionAgenda))
            return false;
        ConfiguracionAgenda that = (ConfiguracionAgenda) o;
        return Objects.equals(id, that.id);
    }

    /**
     * @brief Calcula el código hash de la configuración.
     *
     * @details
     * El hash se calcula únicamente a partir del campo {@code id}, de forma
     * coherente con {@link #equals(Object)}.
     *
     * @return El código hash de la configuración.
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}