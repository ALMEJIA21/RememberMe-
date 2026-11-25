package com.rememberme.app.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomAuthSuccessHandler customAuthSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // Recursos públicos y estáticos
                .requestMatchers(
                    "/", "/index", "/auth/**", "/error",
                    "/css/**", "/js/**", "/images/**",
                    "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**"
                ).permitAll()

                // Control estricto por rol
                .requestMatchers("/cuidador/**").hasAuthority("ROL_CUIDADOR")
                .requestMatchers("/paciente/**").hasAuthority("ROL_PACIENTE")
                .requestMatchers("/admin/**").hasAuthority("ROL_ADMIN")

                // Guías: lectura autenticada, publicación exclusiva del administrador
                .requestMatchers(HttpMethod.POST, "/guias/publicar").hasAuthority("ROL_ADMIN")
                .requestMatchers("/guias/**").authenticated()

                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/auth/login")
                .loginProcessingUrl("/auth/login")
                .usernameParameter("correo")
                .passwordParameter("contrasena")
                .successHandler(customAuthSuccessHandler)
                .failureUrl("/auth/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            );

        return http.build();
    }
}