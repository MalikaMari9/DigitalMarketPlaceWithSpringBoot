package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.AdminNotification;
import com.example.demo.entity.AdminNotification.NotificationStatus;

public interface AdminNotificationRepository extends JpaRepository<AdminNotification, Long> {

	// ✅ Fetch admin-related notifications in descending order
	List<AdminNotification> findByTypeInOrderByCreatedAtDesc(List<AdminNotification.NotificationType> types);

	// ✅ Count unread notifications for admin
	int countByTypeInAndStatus(List<AdminNotification.NotificationType> types, NotificationStatus status);

	// ✅ Fetch only unread admin notifications
	List<AdminNotification> findByTypeInAndStatusOrderByCreatedAtDesc(List<AdminNotification.NotificationType> types,
			NotificationStatus status);
}
