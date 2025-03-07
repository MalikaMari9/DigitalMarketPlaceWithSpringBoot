package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.demo.entity.Announcement;
import com.example.demo.repository.AnnouncementRepository;

@Controller
@RequestMapping("/announcements")
public class AnnouncementController {

	@Autowired
	private AnnouncementRepository announcementRepository;

	// ✅ 1. Show all announcements
	@GetMapping
	public String showAnnouncements(Model model) {
		List<Announcement> announcements = announcementRepository.findAll();
		model.addAttribute("announcements", announcements);
		return "admin/announcement"; // Updated path to match Thymeleaf structure
	}

	// ✅ 2. Show the form to add a new announcement
	@GetMapping("/add")
	public String showAddAnnouncementForm(Model model) {
		model.addAttribute("announcement", new Announcement());
		return "admin/add-announcement"; // Updated path to match Thymeleaf structure
	}

	// ✅ 3. Save a new announcement
	@PostMapping("/add")
	public String addAnnouncement(@ModelAttribute Announcement announcement) {
		announcement.setUpdatedAt(LocalDateTime.now());
		announcementRepository.save(announcement);
		return "redirect:/announcements"; // Redirect back to the list page
	}

	// ✅ 4. Show the form to edit an existing announcement
	@GetMapping("/edit/{id}")
	public String showEditAnnouncementForm(@PathVariable Long id, Model model) {
		Announcement announcement = announcementRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid announcement ID: " + id));
		model.addAttribute("announcement", announcement);
		return "admin/edit-announcement"; // Updated path to match Thymeleaf structure
	}

	// ✅ 5. Update an existing announcement
	@PostMapping("/update/{id}")
	public String updateAnnouncement(@PathVariable Long id, @ModelAttribute Announcement updatedAnnouncement) {
		Announcement existingAnnouncement = announcementRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Invalid announcement ID: " + id));

		existingAnnouncement.setTitle(updatedAnnouncement.getTitle());
		existingAnnouncement.setPriority(updatedAnnouncement.getPriority());
		existingAnnouncement.setContent(updatedAnnouncement.getContent());
		existingAnnouncement.setUpdatedAt(LocalDateTime.now());

		announcementRepository.save(existingAnnouncement);
		return "redirect:/announcements";
	}

	// ✅ 6. Delete an announcement
	@GetMapping("/delete/{id}")
	public String deleteAnnouncement(@PathVariable Long id) {
		announcementRepository.deleteById(id);
		return "redirect:/announcements";
	}
}