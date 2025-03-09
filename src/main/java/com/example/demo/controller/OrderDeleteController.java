package com.example.demo.controller;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.repository.OrderDeleteRepository;
import com.example.demo.repository.OrderRepository;

@RestController
@RequestMapping("/api/order")
public class OrderDeleteController {

    @Autowired
    private OrderDeleteRepository orderDeleteRepository;

    @Autowired
    private OrderRepository orderRepository;

    @DeleteMapping("/delete/{receiptID}")
    @Transactional
    public ResponseEntity<?> deleteOrderByReceipt(@PathVariable Long receiptID) {
        System.out.println("🛠️ Received delete request for Receipt ID: " + receiptID);

        boolean isDeleted = orderDeleteRepository.deleteOrderByReceiptID(receiptID);
        
        if (isDeleted) {
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("message", "Receipt not found or already deleted."));
        }
    }
}
