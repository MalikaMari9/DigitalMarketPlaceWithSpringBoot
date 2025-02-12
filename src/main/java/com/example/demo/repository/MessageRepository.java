package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Message;
import com.example.demo.entity.User;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

	// Fetch messages between two users in chronological order
	List<Message> findBySenderAndReceiverOrSenderAndReceiverOrderByCreatedAtAsc(User sender1, User receiver1,
			User sender2, User receiver2);
}
