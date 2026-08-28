package com.example.demo.repository;

import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends org.springframework.data.jpa.repository.JpaRepository<User, Long> {
    java.util.Optional<User> findByUsernameIgnoreCase(String username);

    java.util.Optional<User> findByIdAndStatus(Long userId, Integer status);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select user from User user where user.id = :userId and user.status = :status")
    java.util.Optional<User> findByIdAndStatusForUpdate(
            @Param("userId") Long userId, @Param("status") Integer status);

    boolean existsByUsernameIgnoreCase(String username);

    Page<User> findByStatus(Integer status, Pageable pageable);

    Page<User> findByRoleAndStatus(Role role, Integer status, Pageable pageable);

    @Query("""
            select user from User user
            where (:keyword is null
                or lower(user.username) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(user.nickname, '')) like lower(concat('%', :keyword, '%'))
                or lower(coalesce(user.email, '')) like lower(concat('%', :keyword, '%'))
                or user.phone like concat('%', :keyword, '%'))
              and (:status is null or user.status = :status)
              and (:role is null or user.role = :role)
            """)
    Page<User> searchForAdmin(
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("role") Role role,
            Pageable pageable);

    long countByRoleAndStatus(Role role, Integer status);
}
