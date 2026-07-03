package com.valentin.tu_cv_spring_bot.TuCv.conroller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
public class AdminAuthController {

    @Value("${admin.secret:}")
    private String adminSecret;

    @GetMapping("/login")
    public String login() {
        return "admin-login";
    }

    @PostMapping("/auth")
    public String auth(@RequestParam String password,
                       @RequestParam(required = false, defaultValue = "/productos") String redirect,
                       HttpServletResponse res,
                       Model model) {
        if (!adminSecret.equals(password)) {
            model.addAttribute("error", "Contrase�a incorrecta");
            return "admin-login";
        }

        String hash;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            hash = HexFormat.of().formatHex(md.digest(adminSecret.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            model.addAttribute("error", "Error interno");
            return "admin-login";
        }

        Cookie cookie = new Cookie("admin_token", hash);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(30 * 24 * 60 * 60);
        res.addCookie(cookie);

        return "redirect:" + redirect;
    }
}
