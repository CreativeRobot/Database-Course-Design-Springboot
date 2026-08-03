package com.example.demo.repository;

import com.example.demo.entity.Author;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthorRepository extends JpaRepository<Author, Long> {
    Page<Author> findByNameContainingIgnoreCase(String name, Pageable pageable);

    List<Author> findByNameIgnoreCase(String name);

    List<Author> findByCountryIgnoreCaseOrderByNameAsc(String country);
}