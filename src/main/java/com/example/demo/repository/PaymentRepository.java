package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.demo.entity.Payment;
import com.example.demo.entity.Receipt;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Payment findByReceipt(Receipt receipt);

}
