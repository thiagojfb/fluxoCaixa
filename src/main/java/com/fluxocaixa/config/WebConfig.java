package com.fluxocaixa.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    // CORS é gerenciado pelo SecurityConfig via CorsConfigurationSource
}
