package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Item;
import com.example.demo.entity.Notification;
import com.example.demo.entity.User;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

	// ✅ Find all notifications for a user (latest first)
	List<Notification> findByUserOrderByCreatedAtDesc(User user);

	// ✅ Find only unread notifications
	List<Notification> findByUserAndSeenFalseOrderByCreatedAtDesc(User user);

	void deleteAllByItem(Item item);
}
