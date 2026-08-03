package com.example.demo.repository;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByIdAndStatus(Long userId, Integer status);

    boolean existsByUsernameIgnoreCase(String username);

    Page<User> findByStatus(Integer status, Pageable pageable);

    Page<User> findByRoleAndStatus(Role role, Integer status, Pageable pageable);
}