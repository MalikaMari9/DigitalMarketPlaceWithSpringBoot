package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Announcement;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

	List<Announcement> findAllByOrderByPriorityDescCreatedAtDesc();
	// Additional custom queries can be added here if needed
}
