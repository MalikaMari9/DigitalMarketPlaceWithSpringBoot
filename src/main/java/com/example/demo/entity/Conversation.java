package com.example.demo.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "conversation_tbl")
public class Conversation {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "conversationId")
	private Long conversationID;

	@Column(nullable = false)
	private int senderID;

	@Column(nullable = false)
	private int receiverID;

	@Column(nullable = false)
	private String lastMessage;

	@Column(nullable = false)
	private boolean isLastMessageRead = false;

	@Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt = LocalDateTime.now();

	@Column(nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime updatedAt = LocalDateTime.now();

	// Constructors
	protected Conversation() {
	}

	@Column(nullable = false)
	private int lastMessageSenderID;

	// Add to constructor
	public Conversation(int senderID, int receiverID, String lastMessage) {
		this.senderID = senderID;
		this.receiverID = receiverID;
		this.lastMessage = lastMessage;
		this.lastMessageSenderID = senderID; // Add this line
		this.isLastMessageRead = false;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}

	// Add getter and setter
	public int getLastMessageSenderID() {
		return lastMessageSenderID;
	}

	public void setLastMessageSenderID(int lastMessageSenderID) {
		this.lastMessageSenderID = lastMessageSenderID;
	}

	// Getters and Setters
	public Long getConversationID() {
		return conversationID;
	}

	public void setConversationID(Long conversationID) {
		this.conversationID = conversationID;
	}

	public int getSenderID() {
		return senderID;
	}

	public void setSenderID(int senderID) {
		this.senderID = senderID;
	}

	public int getReceiverID() {
		return receiverID;
	}

	public void setReceiverID(int receiverID) {
		this.receiverID = receiverID;
	}

	public String getLastMessage() {
		return lastMessage;
	}

	public void setLastMessage(String lastMessage) {
		this.lastMessage = lastMessage;
	}

	public boolean isLastMessageRead() {
		return isLastMessageRead;
	}

	public void setLastMessageRead(boolean isLastMessageRead) {
		this.isLastMessageRead = isLastMessageRead;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}
}
