package com.valentin.tu_cv_spring_bot.TuCv.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class CsrfTokenAdvice {

    @ModelAttribute("csrf_token")
    public String csrfToken(HttpServletRequest req) {
        var cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("admin_token".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
