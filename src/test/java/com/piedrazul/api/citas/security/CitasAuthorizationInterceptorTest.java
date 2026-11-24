package com.piedrazul.api.citas.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.server.ResponseStatusException;

import com.piedrazul.api.iam.domain.RoleEnum;
import com.piedrazul.api.iam.provider.TokenProvider;

@ExtendWith(MockitoExtension.class)
class CitasAuthorizationInterceptorTest {

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private CitasAuthorizationInterceptor interceptor;

    @Test
    void preHandle_sugerenciasConTokenTerapista_permiteAcceso() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/citas/pacientes/sugerencias");
        request.addHeader("Authorization", "Bearer terapista-token");
        when(tokenProvider.validateToken("terapista-token")).thenReturn(true);
        when(tokenProvider.getRoleFromToken("terapista-token")).thenReturn(RoleEnum.THERAPIST);

        boolean allowed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(allowed).isTrue();
    }

    @Test
    void preHandle_sugerenciasConTokensMedicoAdminAgendador_permiteAcceso() {
        for (RoleEnum rol : new RoleEnum[] { RoleEnum.MEDICO, RoleEnum.ADMIN, RoleEnum.AGENDADOR }) {
            String token = "token-" + rol.name();
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/citas/pacientes/sugerencias");
            request.addHeader("Authorization", "Bearer " + token);
            when(tokenProvider.validateToken(token)).thenReturn(true);
            when(tokenProvider.getRoleFromToken(token)).thenReturn(rol);

            boolean allowed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

            assertThat(allowed).isTrue();
        }
    }

    @Test
    void preHandle_sugerenciasConTokenPaciente_retornaForbidden() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/citas/pacientes/sugerencias");
        request.addHeader("Authorization", "Bearer paciente-token");
        when(tokenProvider.validateToken("paciente-token")).thenReturn(true);
        when(tokenProvider.getRoleFromToken("paciente-token")).thenReturn(RoleEnum.PATIENT);

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void preHandle_sinToken_retornaUnauthorized() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/citas/pacientes/sugerencias");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void preHandle_preflightOptions_permiteAcceso() {
        MockHttpServletRequest request = new MockHttpServletRequest("OPTIONS", "/api/citas/pacientes/sugerencias");

        boolean allowed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(allowed).isTrue();
    }

    @Test
    void preHandle_cancelarConTokenPaciente_permiteAcceso() {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/citas/7/cancelar");
        request.addHeader("Authorization", "Bearer paciente-token");
        when(tokenProvider.validateToken("paciente-token")).thenReturn(true);
        when(tokenProvider.getRoleFromToken("paciente-token")).thenReturn(RoleEnum.PATIENT);

        boolean allowed = interceptor.preHandle(request, new MockHttpServletResponse(), new Object());

        assertThat(allowed).isTrue();
    }

    @Test
    void preHandle_cancelarConOtrosRoles_retornaForbidden() {
        for (RoleEnum rol : new RoleEnum[] { RoleEnum.ADMIN, RoleEnum.AGENDADOR, RoleEnum.MEDICO, RoleEnum.THERAPIST }) {
            String token = "token-" + rol.name();
            MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/citas/7/cancelar");
            request.addHeader("Authorization", "Bearer " + token);
            when(tokenProvider.validateToken(token)).thenReturn(true);
            when(tokenProvider.getRoleFromToken(token)).thenReturn(rol);

            assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                    .isInstanceOf(ResponseStatusException.class)
                    .extracting("statusCode")
                    .isEqualTo(HttpStatus.FORBIDDEN);
        }
    }

    @Test
    void preHandle_cancelarSinToken_retornaUnauthorized() {
        MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/citas/7/cancelar");

        assertThatThrownBy(() -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
