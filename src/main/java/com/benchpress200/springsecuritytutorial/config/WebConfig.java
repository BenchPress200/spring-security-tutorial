package com.benchpress200.springsecuritytutorial.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PATCH", "DELETE", "OPTIONS")
                .allowedOrigins("http://localhost:3000")
                .exposedHeaders("Set-Cookie")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600); // Pre-flight 요청(OPTIONS)의 결과를 3600초(1시간) 동안 캐싱함
    }
}

