package com.example.demo.service;

import com.example.demo.dto.LoginRequestDTO;
import com.example.demo.dto.UserRequestDTO;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor    // final 필드나 @NonNull이 붙은 필드만을 인자로 받는 생성자를 자동으로 생성
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public void signUp(UserRequestDTO userRequestDTO) {
        if (userRepository.findByUsername(userRequestDTO.getUsername()).isPresent()) {  // isPresent() - null 검증
            // 이미 존재하는 사용자
            throw new RuntimeException("이미 존재하는 사용자 입니다.");
        }

        User user = User.builder()
                .username(userRequestDTO.getUsername())
                .password(passwordEncoder.encode(userRequestDTO.getPassword()))
                .role("ROLE_USER")
                .build();

        userRepository.save(user);
    }

    public String login(LoginRequestDTO loginRequestDTO) {
        User user = userRepository
                .findByUsername(loginRequestDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        if (!passwordEncoder.matches(loginRequestDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return jwtTokenProvider.generateToken(user.getUsername());
    }
}
