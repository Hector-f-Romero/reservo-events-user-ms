package com.hector.crud.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Deshabilitar CSRF para APIs REST
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/**").permitAll() // Permitir acceso a tus endpoints API
                        // Puedes agregar más reglas específicas aquí
                        .anyRequest().permitAll() // El resto requiere autenticación
                )
                .formLogin(form -> form.disable())
                .httpBasic(httpBasic -> {
                })

        ;

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // DelegatingPasswordEncoder is more flexible and can adopt different hashing
        // methods.
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
