package com.piedrazul.api.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.piedrazul.api.citas.security.AuthenticatedUserArgumentResolver;
import com.piedrazul.api.scheduling.security.SchedulingAuthorizationInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver;
    private final SchedulingAuthorizationInterceptor schedulingAuthorizationInterceptor;
    private final String frontendOrigin;

    public WebConfig(AuthenticatedUserArgumentResolver authenticatedUserArgumentResolver,
                     SchedulingAuthorizationInterceptor schedulingAuthorizationInterceptor,
                     @Value("${app.cors.allowed-origin:http://localhost:4200}") String frontendOrigin) {
        this.authenticatedUserArgumentResolver = authenticatedUserArgumentResolver;
        this.schedulingAuthorizationInterceptor = schedulingAuthorizationInterceptor;
        this.frontendOrigin = frontendOrigin;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
            .allowedOrigins(frontendOrigin)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
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
    }
}