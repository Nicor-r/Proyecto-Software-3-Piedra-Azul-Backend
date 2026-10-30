package com.piedrazul.api.citas.domain;

import java.util.Objects;

/**
 * Value Object que representa el numero de cedula de un paciente (HE-05).
 *
 * Invariantes (HU-5):
 * - Escenario 4: no puede ser nula ni vacia.
 * - Escenario 5: solo numeros, entre 6 y 10 digitos.
 *
 * Si se logra crear una instancia, la cedula ya es valida. Es inmutable.
 */
public final class Cedula {

    public static final String MENSAJE_CEDULA_VACIA = "Debe ingresar el número de cédula";
    public static final String MENSAJE_FORMATO_INVALIDO = "La cédula debe contener solo números, entre 6 y 10 dígitos";

    // Solo digitos (\\d), minimo 6 y maximo 10.
    private static final String PATRON_CEDULA = "\\d{6,10}";

    private final String valor;

    public Cedula(String valor) {
        if (valor == null || valor.isBlank()) {
            throw new IllegalArgumentException(MENSAJE_CEDULA_VACIA);
        }

        String limpio = valor.trim();
        if (!limpio.matches(PATRON_CEDULA)) {
            throw new IllegalArgumentException(MENSAJE_FORMATO_INVALIDO);
        }

        this.valor = limpio;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Cedula))
            return false;
        Cedula that = (Cedula) o;
        return valor.equals(that.valor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }

    @Override
    public String toString() {
        return valor;
    }
}