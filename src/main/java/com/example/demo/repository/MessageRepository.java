package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Message;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
	List<Message> findBySenderIDAndReceiverIDOrSenderIDAndReceiverIDOrderByCreatedAtAsc(int senderID, int receiverID,
			int receiverID2, int senderID2);
}
