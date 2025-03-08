package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUserID(Long userID);

	Optional<User> findByUsername(String username);

	List<User> findByUsernameContainingIgnoreCase(String username);

	Optional<User> findByEmail(String email);

	boolean existsById(Long id);

	void deleteUserDependencies(Long id);

}