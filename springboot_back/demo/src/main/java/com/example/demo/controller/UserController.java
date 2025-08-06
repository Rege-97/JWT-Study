package com.example.demo.controller;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.LoginResponseDTO;
import com.example.demo.dto.UserRequestDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.UserService;
import com.example.demo.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;

    @PostMapping("/signup")
    public String signup(@RequestBody UserRequestDTO userRequestDTO) {
        userService.signUp(userRequestDTO);
        return "회원가입 성공!";
    }

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO loginRequestDTO) {
        return userService.login(loginRequestDTO);
    }

    @PostMapping("/refresh")
    public LoginResponseDTO refresh(@RequestHeader("Authorization") String refreshToken) {
        String token = refreshToken.replace("Bearer ", "");

        if (!jwtTokenProvider.validateToken(token)) {
            throw new RuntimeException("Refresh token이 유효하지 않습니다.");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자가 존재하지 않습니다."));

        if (!token.equals(user.getRefreshToken())) {
            throw new RuntimeException("서버에 저장된 Refresh Token과 일치하지 않습니다.");
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(username);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(username);

        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return ResponseEntity.ok(new LoginResponseDTO(newAccessToken, newRefreshToken)).getBody();
    }

    @PostMapping("logout")
    public String logout(@RequestHeader("Authorization") String accessToken) {
        String token=accessToken.replace("Bearer ", "");
        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username).orElseThrow(()-> new RuntimeException("사용자가 존재하지 않습니다."));

        user.setRefreshToken(null);
        userRepository.save(user);

        return "로그아웃 성공";
    }
}
