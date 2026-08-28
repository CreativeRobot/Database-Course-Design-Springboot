package com.example.demo.repository;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByIdAndStatus(Long userId, Integer status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :userId and user.status = :status")
    Optional<User> findByIdAndStatusForUpdate(
            @Param("userId") Long userId, @Param("status") Integer status);

    boolean existsByUsernameIgnoreCase(String username);

    Page<User> findByStatus(Integer status, Pageable pageable);

    Page<User> findByRoleAndStatus(Role role, Integer status, Pageable pageable);
}