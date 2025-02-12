package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "message_tbl") // Renamed to maintain consistency with your database naming
public class Message {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "messageid")
	private Long messageID;

	@ManyToOne
	@JoinColumn(name = "senderID", nullable = false, referencedColumnName = "userID", foreignKey = @ForeignKey(name = "fk_message_sender"))
	private User sender;

	@ManyToOne
	@JoinColumn(name = "receiverID", nullable = false, referencedColumnName = "userID", foreignKey = @ForeignKey(name = "fk_message_receiver"))
	private User receiver;

	@Column(nullable = true) // Allow null for messages that contain only an image
	private String message;

	@Column(nullable = true) // Store image filename instead of full path
	private String imageUrl;

	@Column(nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt;

	@PrePersist
	protected void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

	// Constructors
	public Message() {
	}

	public Message(User sender, User receiver, String message, String imageUrl) {
		this.sender = sender;
		this.receiver = receiver;
		this.message = message;
		this.imageUrl = imageUrl;
		this.createdAt = LocalDateTime.now();
	}

	// Getters and Setters
	public Long getMessageID() {
		return messageID;
	}

	public void setMessageID(Long messageID) {
		this.messageID = messageID;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public User getReceiver() {
		return receiver;
	}

	public void setReceiver(User receiver) {
		this.receiver = receiver;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}
