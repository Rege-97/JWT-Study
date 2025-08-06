package com.example.demo.config;

import com.example.demo.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)// CSRF 보호 끔(REST API 서버 에서는 꺼도 됨)
                .formLogin(AbstractHttpConfigurer::disable) // Spring Security 기본 로그인 폼 UI 비활성화
                .httpBasic(Customizer.withDefaults())   // JWT 등 다른 인증 방식 사용
                .authorizeHttpRequests(auth -> auth // 들어오는 요청 URL에 따라 허용할지 인증 필요한지 설정
                        .requestMatchers("/api/private/**").hasAnyRole("USER","ADMIN") // 인증 필요
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .anyRequest().denyAll()) // 나머지 모든 요청은 차단
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  // Spring Security에 jwt 필터가 먼저 실행되도록 추가


        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // CORS(Cross-Origin Resource Sharing)는 다른 출처(도메인)에서 온 요청을 허용할지 말지를 결정하는 보안 정책

        // Cors 옵션을 설정할 수 있는 객체
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.addAllowedOrigin("http://localhost:5173"); // 이 출처에서 오는 요청만 허용(React)
        configuration.addAllowedHeader("*");                     // 어떤 헤더 요청이든 다 허용
        configuration.addAllowedMethod("*");                     // 어떤 HTTP 메서드든 다 허용
        configuration.setAllowCredentials(true);                 // 쿠키, 인증정보 등의 자격증명을 같이 보낼 수 있도록 허용

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 URL(/**)에 대해 위에서 설정한 CORS 정책을 적용
        return source;
    }
}
