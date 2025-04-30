package me.cho.snackball.global.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsMvcConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        corsRegistry.addMapping("/**")
                .allowedOrigins("https://skaa.p-e.kr")
                .allowedMethods("GET", "POST", "PUT", "DELETE") // 요청 방식도 확인
                .allowedHeaders("*") // 모든 헤더 허용
                .allowCredentials(true); // 자격 증명 허용
    }
}
