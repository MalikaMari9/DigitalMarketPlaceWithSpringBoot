package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Conversation;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
	Optional<List<Conversation>> findBySenderID(int senderID); // Changed from senderId to senderID

	List<Conversation> findBySenderIDOrReceiverID(int senderID, int receiverID);
}
