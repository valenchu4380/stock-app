package com.valentin.tu_cv_spring_bot.TuCv.config;

import com.valentin.tu_cv_spring_bot.TuCv.mODEL.Linea;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class LineaConverter implements Converter<String, Linea> {
    @Override
    public Linea convert(String source) {
        return Linea.fromString(source);
    }
}
