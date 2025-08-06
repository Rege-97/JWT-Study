package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)// CSRF 보호 끔(REST API 서버 에서는 꺼도 됨)
                .formLogin(AbstractHttpConfigurer::disable) // Spring Security 기본 로그인 폼 UI 비활성화
                .httpBasic(Customizer.withDefaults())   // JWT 등 다른 인증 방식 사용
                .authorizeHttpRequests(auth -> auth // 들어오는 요청 URL에 따라 허용할지 인증 필요한지 설정
                        .requestMatchers("/api/public/**").permitAll()  // 허용
                        .requestMatchers("/api/private/**").authenticated() // 인증 필요
                        .anyRequest().denyAll() // 나머지 모든 요청은 차단
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
