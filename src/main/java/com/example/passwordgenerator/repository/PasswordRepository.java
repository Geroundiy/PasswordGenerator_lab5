package com.example.passwordgenerator.repository;

import com.example.passwordgenerator.entity.Password;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PasswordRepository extends JpaRepository<Password, Long> {

    @Query("SELECT p FROM Password p JOIN p.tags t WHERE t.name = :tagName")
    List<Password> findPasswordsByTagName(@Param("tagName") String tagName);
}