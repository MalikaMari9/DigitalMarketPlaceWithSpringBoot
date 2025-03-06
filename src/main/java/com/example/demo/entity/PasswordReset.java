package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "password_resets_tbl")
public class PasswordReset {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String token;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private LocalDateTime expiryDate;

	private boolean used = false;

	// Default constructor required by JPA
	protected PasswordReset() {
	}

	// Main constructor for creating new password reset tokens
	public PasswordReset(User user, String token, LocalDateTime expiryDate) {
		this.user = user;
		this.token = token;
		this.expiryDate = expiryDate;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public void setExpiryDate(LocalDateTime expiryDate) {
		this.expiryDate = expiryDate;
	}

	public Long getId() {
		return id;
	}

	public String getToken() {
		return token;
	}

	public User getUser() {
		return user;
	}

	public LocalDateTime getExpiryDate() {
		return expiryDate;
	}

	public boolean isUsed() {
		return used;
	}

	public void setUsed(boolean used) {
		this.used = used;
	}
}