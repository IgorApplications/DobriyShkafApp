package com.iapp.iapp_messenger.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(AbstractHttpConfigurer::disable) // для REST
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // login/register открыты
                        .requestMatchers("/api/families-admin/**").permitAll() // TODO
                        .requestMatchers("/ws/**").permitAll()   // ИЗ HTTP убирает WS
                        .anyRequest().authenticated()
                )
                // выполни JwtAuthFilter ПЕРЕД стандартным фильтром логина
                // Spring Security по умолчанию ждёт:
                //
                // либо форму логина
                // либо basic auth
                //
                // А ты используешь JWT, значит:
                //
                // нужно перехватывать каждый HTTP запрос
                // вытаскивать токен
                // аутентифицировать пользователя
                //
                // И это делает твой фильтр.
                //
                // 2) Что делает JWT Filter (по сути)
                //
                // Твой JwtAuthFilter — это:
                //
                // "middleware" между запросом и контроллером
                //
                // Логика:
                // Приходит HTTP запрос
                // Фильтр смотрит заголовок:
                // Authorization: Bearer eyJhbGciOi...
                // Если токен есть:
                // извлекает login
                // проверяет валидность
                // кладёт пользователя в SecurityContext
                // SecurityContextHolder.getContext().setAuthentication(auth);
                // Дальше Spring думает:
                //
                // "пользователь уже залогинен"
                /** для REST см. JwtAuthFilter*/
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable) // убираем HTML login
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }
}
