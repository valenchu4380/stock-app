package com.valentin.tu_cv_spring_bot.TuCv.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final LineaConverter lineaConverter;

    public WebConfig(LineaConverter lineaConverter) {
        this.lineaConverter = lineaConverter;
    }

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(lineaConverter);
    }
}
