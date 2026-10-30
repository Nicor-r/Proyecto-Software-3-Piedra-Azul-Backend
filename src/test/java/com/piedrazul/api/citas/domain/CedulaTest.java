package com.piedrazul.api.citas.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CedulaTest {

    @Test
    void cedulaDe10Digitos_seCreaCorrectamente() {
        Cedula cedula = new Cedula("1061000001");

        assertThat(cedula.getValor()).isEqualTo("1061000001");
    }

    @Test
    void cedulaDe6Digitos_limiteInferior_esValida() {
        Cedula cedula = new Cedula("123456");

        assertThat(cedula.getValor()).isEqualTo("123456");
    }

    @Test
    void cedulaConEspaciosAlrededor_seGuardaSinEspacios() {
        Cedula cedula = new Cedula("  1061000001  ");

        assertThat(cedula.getValor()).isEqualTo("1061000001");
    }

    @Test
    void cedulaNula_lanzaMensajeCedulaVacia() {
        assertThatThrownBy(() -> new Cedula(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Cedula.MENSAJE_CEDULA_VACIA);
    }

    @Test
    void cedulaEnBlanco_lanzaMensajeCedulaVacia() {
        assertThatThrownBy(() -> new Cedula("   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Cedula.MENSAJE_CEDULA_VACIA);
    }

    @Test
    void cedulaCon5Digitos_lanzaMensajeFormatoInvalido() {
        assertThatThrownBy(() -> new Cedula("12345"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Cedula.MENSAJE_FORMATO_INVALIDO);
    }

    @Test
    void cedulaCon11Digitos_lanzaMensajeFormatoInvalido() {
        assertThatThrownBy(() -> new Cedula("12345678901"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Cedula.MENSAJE_FORMATO_INVALIDO);
    }

    @Test
    void cedulaConLetras_lanzaMensajeFormatoInvalido() {
        assertThatThrownBy(() -> new Cedula("10610abc01"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Cedula.MENSAJE_FORMATO_INVALIDO);
    }

    @Test
    void cedulaConSimbolos_lanzaMensajeFormatoInvalido() {
        assertThatThrownBy(() -> new Cedula("1061-00001"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(Cedula.MENSAJE_FORMATO_INVALIDO);
    }
}