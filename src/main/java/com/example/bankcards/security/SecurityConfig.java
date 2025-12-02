package com.example.bankcards.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // Статика (CSS, JS) доступна всем
                        .requestMatchers("/static/**", "/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()

                        // Публичные страницы и эндпоинты авторизации
                        .requestMatchers("/", "/login", "/register").permitAll()
                        .requestMatchers("/api/auth/**", "/v3/**", "/swagger-ui/**").permitAll()

                        // Админская зона
                        .requestMatchers("/admin/**", "/api/admin/**").hasAuthority("ROLE_ADMIN")

                        // Пользовательская зона (Дашборд доступен и юзеру, и админу)
                        .requestMatchers("/dashboard", "/api/user/**").hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                // ВАЖНО: Обработка ошибок для редиректов в браузере
                .exceptionHandling(e -> e
                        .authenticationEntryPoint((request, response, authException) -> {
                            String uri = request.getRequestURI();
                            // Если запрос к API - возвращаем 401 JSON
                            if (uri.startsWith("/api")) {
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, authException.getMessage());
                            } else {
                                // Если запрос к странице - редирект на форму входа
                                response.sendRedirect("/login");
                            }
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            String uri = request.getRequestURI();
                            if (uri.startsWith("/api")) {
                                response.sendError(HttpServletResponse.SC_FORBIDDEN, accessDeniedException.getMessage());
                            } else {
                                // Если прав нет (например, юзер лезет в админку) - кидаем на общий дашборд
                                response.sendRedirect("/dashboard");
                            }
                        })
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}