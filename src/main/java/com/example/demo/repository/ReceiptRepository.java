package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Receipt;
import com.example.demo.entity.User;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

	List<Receipt> findByBuyer(User user);

	List<Receipt> findBySeller(User seller);
}
