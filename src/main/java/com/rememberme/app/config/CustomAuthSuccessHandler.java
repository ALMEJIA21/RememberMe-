package com.rememberme.app.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());

        if (roles.contains("ROL_ADMIN")) {
            response.sendRedirect(request.getContextPath() + "/admin/resumen");
        } else if (roles.contains("ROL_CUIDADOR")) {
            response.sendRedirect(request.getContextPath() + "/cuidador/panel");
        } else if (roles.contains("ROL_PACIENTE")) {
            response.sendRedirect(request.getContextPath() + "/paciente/mi-plan");
        } else {
            response.sendRedirect(request.getContextPath() + "/");
        }
    }
}