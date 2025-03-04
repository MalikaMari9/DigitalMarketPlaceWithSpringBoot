package com.example.demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Delivery;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {
	Optional<Delivery> findByReceiptReceiptID(Long receiptID);
}
