package com.example.demo.security;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter { // 	HTTP 요청 1번당 1번만 실행되는 커스텀 필터를 만들기 위해 상속

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,   // 필터 체인으로 넘기기 위해 리퀘스트, 리스폰스 객체 받음
                                    FilterChain filterChain) throws ServletException, IOException { // 현재 필터 이후 남아있는 다음 필터들을 실행할 수 있게 해주는 객체
        String token = resolveToken(request);
        if (token != null && jwtTokenProvider.validateToken(token)) {
            String username = jwtTokenProvider.getUsernameFromToken(token);
            User user = userRepository
                    .findByUsername(username)
                    .orElse(null);

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (user != null) {
                // 인증 객체 생성(인증 주체, 인증 자격정보(비밀번호나 인증 후라면 null), 권한 목록
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(user, null, userDetails.getAuthorities());
                // 인증 객체를 Spring Security에 로그인된 객체로 설정
                SecurityContextHolder   // Spring Security의 보안정보를 담고 있는 저장소
                        .getContext()   // 보안 Context 꺼내기
                        .setAuthentication(authentication); // 그 보안 Context에 인증 객체 설정
            }
        }

        filterChain.doFilter(request, response); // 다음 필터 또는 DispatcherServlet으로 요청 전달
    }

    // Authorization 헤더에서 Bearer {토큰} 형식 중 토큰만 꺼내는 함수
    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
