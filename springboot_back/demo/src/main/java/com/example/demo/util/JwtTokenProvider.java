package com.example.demo.util;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256); // 비밀키 생성
    private final long expiration = 1000L * 60 * 60;// 토큰 만료시간 (1시간

    /**
     * JWT 토큰 생성
     * @param username
     * @return
     */
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // 유저 이름을 토큰에 담는다
                .setIssuedAt(new Date()) // 발급 시각
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) // 만료 시각
                .signWith(key) // 서명 (변조 방지)
                .compact(); // JWT 문자열로 변환
    }

    /**
     * JWT 로그인(복호화)
     * @param token
     * @return
     */
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)                      // 토큰 서명 검증을 위한 키 설정
                .build()
                .parseClaimsJws(token)                   // 토큰을 파싱하고 유효성 검사
                .getBody()                               // Claims(내용물) 가져오기
                .getSubject();                           // subject (즉 username) 꺼냄
    }

    /**
     * 토큰 검증
     * @param token
     * @return
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

}
