package com.piedrazul.api.scheduling.domain;

import java.util.Objects;

/**
 * ConfiguracionAgenda - Entidad de dominio que agrupa los 4 Value Obj para un
 * medico/terapista especifico
 * 
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ConfiguracionAgenda))
            return false;
        ConfiguracionAgenda that = (ConfiguracionAgenda) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

}