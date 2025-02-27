package com.example.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.AdminNotification;
import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.AdminNotificationRepository;
import com.example.demo.repository.NotificationRepository;

import jakarta.servlet.http.HttpSession;

@Controller

public class NotificationController {

	// Notification

	@Autowired
	private NotificationRepository notificationRepo;

	// ✅ Get all notifications for logged-in user
	@GetMapping("/notifications")
	public String viewNotifications(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");

		if (user == null) {
			return "redirect:/loginPage"; // ✅ Redirect if user is not logged in
		}

		// ✅ Fetch all notifications for the user
		List<Notification> notifications = notificationRepo.findByUserOrderByCreatedAtDesc(user);
		model.addAttribute("notifications", notifications);

		// ✅ Mark all notifications as seen
		notifications.forEach(noti -> noti.setSeen(true));
		notificationRepo.saveAll(notifications);

		return "notification"; // ✅ Thymeleaf file
	}

	@GetMapping("/notifications/unread-count")
	public ResponseEntity<Map<String, Integer>> getUnreadNotificationCount(HttpSession session) {
		User user = (User) session.getAttribute("user");

		int unreadCount = (user != null) ? notificationRepo.findByUserAndSeenFalseOrderByCreatedAtDesc(user).size() : 0; // Default
																															// to
																															// 0
																															// if
																															// user
																															// is
																															// not
																															// logged
																															// in

		Map<String, Integer> response = new HashMap<>();
		response.put("unreadCount", unreadCount);
		return ResponseEntity.ok(response);
	}

	// ✅ Get only unread notifications for logged-in user
	@GetMapping("/notifications/unread")
	@ResponseBody
	public ResponseEntity<List<Notification>> getUnreadNotifications(HttpSession session) {
		User user = (User) session.getAttribute("user");

		if (user == null) {
			return ResponseEntity.badRequest().build();
		}

		List<Notification> unreadNotifications = notificationRepo.findByUserAndSeenFalseOrderByCreatedAtDesc(user);
		return ResponseEntity.ok(unreadNotifications);
	}

	@Autowired
	private AdminNotificationRepository adminNotificationRepo;

	@GetMapping("/admin/notifications")
	public String viewAdminNotifications(HttpSession session, Model model) {
		// ✅ Check if admin is logged in
		Boolean isAdmin = (Boolean) session.getAttribute("admin");
		if (isAdmin == null || !isAdmin) {
			return "redirect:/loginPage"; // Redirect if not an admin
		}

		// ✅ Fetch admin-related notifications
		List<AdminNotification> adminNotifications = adminNotificationRepo.findByTypeInOrderByCreatedAtDesc(List.of(
				AdminNotification.NotificationType.SELLER_APPROVAL, AdminNotification.NotificationType.ITEM_APPROVAL,
				AdminNotification.NotificationType.AUCTION_UPDATE));

		model.addAttribute("adminNotifications", adminNotifications);

		// ✅ Mark notifications as READ
		if (!adminNotifications.isEmpty()) {
			adminNotifications.forEach(noti -> noti.setStatus(AdminNotification.NotificationStatus.READ));
			adminNotificationRepo.saveAll(adminNotifications);
		}

		return "admin/admin-notification"; // ✅ Thymeleaf page for admin notifications
	}

	@GetMapping("/admin/notifications/unread-count")
	public ResponseEntity<Map<String, Integer>> getUnreadAdminNotificationCount(HttpSession session) {
		Boolean isAdmin = (Boolean) session.getAttribute("admin");

		int unreadCount = (isAdmin != null && isAdmin)
				? adminNotificationRepo.countByTypeInAndStatus(
						List.of(AdminNotification.NotificationType.SELLER_APPROVAL,
								AdminNotification.NotificationType.ITEM_APPROVAL,
								AdminNotification.NotificationType.AUCTION_UPDATE),
						AdminNotification.NotificationStatus.UNREAD)
				: 0;

		return ResponseEntity.ok(Map.of("unreadCount", unreadCount));
	}

	@GetMapping("/admin/notifications/unread")
	@ResponseBody
	public ResponseEntity<List<AdminNotification>> getUnreadAdminNotifications(HttpSession session) {
		Boolean isAdmin = (Boolean) session.getAttribute("admin");

		if (isAdmin == null || !isAdmin) {
			return ResponseEntity.badRequest().build();
		}

		List<AdminNotification> unreadAdminNotifications = adminNotificationRepo
				.findByTypeInAndStatusOrderByCreatedAtDesc(
						List.of(AdminNotification.NotificationType.SELLER_APPROVAL,
								AdminNotification.NotificationType.ITEM_APPROVAL,
								AdminNotification.NotificationType.AUCTION_UPDATE),
						AdminNotification.NotificationStatus.UNREAD);

		return ResponseEntity.ok(unreadAdminNotifications);
	}

}
