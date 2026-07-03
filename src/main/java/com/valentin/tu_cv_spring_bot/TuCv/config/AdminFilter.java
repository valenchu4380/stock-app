package com.valentin.tu_cv_spring_bot.TuCv.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(1)
public class AdminFilter implements Filter {

    @Value("${admin.secret}")
    private String adminSecret;

    private String expectedHash;

    private String hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(input.getBytes());
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private void ensureHash() {
        if (expectedHash == null) {
            expectedHash = hash(adminSecret);
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        String path = req.getRequestURI();
        String method = req.getMethod();

        if (!isAdminPath(path, method)) {
            chain.doFilter(request, response);
            return;
        }

        ensureHash();
        String token = readCookie(req, "admin_token");
        if (token != null && token.equals(expectedHash)) {
            chain.doFilter(request, response);
            return;
        }

        res.sendRedirect("/admin/login");
    }

    private boolean isAdminPath(String path, String method) {
        String p = path.replaceFirst("^/", "");

        if (p.equals("productos/nuevo")) return true;
        if (p.startsWith("productos/editar")) return true;
        if (p.equals("productos/eliminar")) return true;
        if (p.equals("productos/ajustar-stock")) return true;
        if (p.equals("productos/editar-masivo")) return true;
        if (p.equals("productos/actualizar-precios-sub")) return true;
        if (p.startsWith("productos/lineas")) return true;
        if (p.equals("productos/asignar-lineas-pendientes")) return true;
        if (p.equals("productos/movimientos")) return true;
        if (p.equals("productos/dashboard")) return true;
        if (p.equals("productos/detectar-linea")) return true;
        if (p.equals("productos/lineas-por-categoria")) return true;

        if (p.startsWith("productos/compras")) {
            if (p.equals("productos/compras/crear") && "POST".equals(method)) return false;
            return true;
        }

        return false;
    }

    private String readCookie(HttpServletRequest req, String name) {
        var cookies = req.getCookies();
        if (cookies != null) {
            for (var c : cookies) {
                if (c.getName().equals(name)) return c.getValue();
            }
        }
        return null;
    }
}
