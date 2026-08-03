package com.example.demo.repository;

import com.example.demo.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUser_IdOrderByDefaultAddressDescCreateTimeDesc(Long userId);

    Optional<UserAddress> findByIdAndUser_Id(Long addressId, Long userId);

    Optional<UserAddress> findFirstByUser_IdAndDefaultAddressTrue(Long userId);

    long countByUser_Id(Long userId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update UserAddress address
            set address.defaultAddress = false
            where address.user.id = :userId
              and address.defaultAddress = true
            """)
    int clearDefaultAddress(@Param("userId") Long userId);

    @Transactional
    long deleteByIdAndUser_Id(Long addressId, Long userId);
}