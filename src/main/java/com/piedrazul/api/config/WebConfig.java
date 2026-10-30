package com.piedrazul.api.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.http.HttpHeaders;

import com.piedrazul.api.citas.security.AuthenticatedUserArgumentResolver;
import com.piedrazul.api.citas.security.CitasAuthorizationInterceptor;
import com.piedrazul.api.scheduling.security.SchedulingAuthorizationInterceptor;
import com.piedrazul.api.citas.security.MedicoAuthorizationInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;
    private final SchedulingAuthorizationInterceptor schedulingAuthorizationInterceptor;
    private final CitasAuthorizationInterceptor citasAuthorizationInterceptor;   // NUEVO
    private final MedicoAuthorizationInterceptor medicoAuthorizationInterceptor;
    private final String frontendOrigin;

        public WebConfig(AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver,
            SchedulingAuthorizationInterceptor schedulingAuthorizationInterceptor,
            CitasAuthorizationInterceptor citasAuthorizationInterceptor,
            MedicoAuthorizationInterceptor medicoAuthorizationInterceptor,
            @Value("${app.cors.allowed-origin:http://localhost:4200}") String frontendOrigin) {
        this.authenticatedUserArgumentResolver = authenticatedUserArgumentResolver;
        this.schedulingAuthorizationInterceptor = schedulingAuthorizationInterceptor;
        this.citasAuthorizationInterceptor = citasAuthorizationInterceptor;
        this.medicoAuthorizationInterceptor = medicoAuthorizationInterceptor;
        this.frontendOrigin = frontendOrigin;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(frontendOrigin)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders(HttpHeaders.CONTENT_DISPOSITION);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedUserArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(schedulingAuthorizationInterceptor)
                .addPathPatterns("/api/admin/doctors", "/api/admin/doctors/**",
                        "/api/admin/schedule-config", "/api/admin/schedule-config/**");

        registry.addInterceptor(citasAuthorizationInterceptor)
                .addPathPatterns("/api/citas/exportar",
                        "/api/citas/paciente/**");

        registry.addInterceptor(medicoAuthorizationInterceptor)
                .addPathPatterns("/api/medico/**");
    }
}
