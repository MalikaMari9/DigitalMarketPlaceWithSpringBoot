package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.demo.entity.Cart;
import com.example.demo.entity.Item;
import com.example.demo.entity.User;

public interface CartRepository extends JpaRepository<Cart, Long> {

	int countByUser(User user);

	Optional<Cart> findFirstByUserAndItem(User user, Item item);

	@Query("SELECT COUNT(c) FROM Cart c WHERE c.user.userID = :userId")
	int countByUser(@Param("userId") Long userId);

	@Query("SELECT c FROM Cart c JOIN FETCH c.item i JOIN FETCH i.seller WHERE c.user = :user")
	List<Cart> findByUser(@Param("user") User user);

	void deleteById(Long cartID); // ✅ Deletes cart item by

}
