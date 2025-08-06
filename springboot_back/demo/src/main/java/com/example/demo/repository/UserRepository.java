package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> { // JpaRepository<엔티티,id 타입>를 상속하여 jpa 기능 사용
    Optional<User> findByUsername(String username);
}
