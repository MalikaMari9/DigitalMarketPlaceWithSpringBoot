package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.PasswordReset;

public interface PasswordResetRepository extends JpaRepository<PasswordReset, Long> {
	Optional<PasswordReset> findByTokenAndUsedFalse(String token);

	Optional<PasswordReset> findByToken(String token);
}