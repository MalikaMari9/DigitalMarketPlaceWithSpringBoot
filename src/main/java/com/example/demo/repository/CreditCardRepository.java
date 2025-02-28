package com.example.demo.repository;

import com.example.demo.entity.CreditCard;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    // ✅ Find all credit cards for a specific user
    List<CreditCard> findByUser(User user);

    // ✅ Find a specific credit card by last four digits and user
    CreditCard findByUserAndLastFourDigits(User user, String lastFourDigits);

    // ✅ Check if a credit card exists by last four digits and user
    boolean existsByUserAndLastFourDigits(User user, String lastFourDigits);
}
