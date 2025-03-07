package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.demo.entity.User;
import com.example.demo.repository.AddressRepository;

import jakarta.servlet.http.HttpSession;

@ControllerAdvice
public class GlobalModelAttributeController {

	@Autowired
	private AddressRepository addressRepository;

	@ModelAttribute("hasMainAddress")
	public boolean checkSellerMainAddress(HttpSession session) {
		User seller = (User) session.getAttribute("user");

		// ✅ Check if user is a SELLER and has a main address
		if (seller != null && "SELLER".equalsIgnoreCase(seller.getRole())) {
			return addressRepository.existsByUserAndIsMainAddressTrue(seller);
		}

		return true; // Default to true for non-sellers
	}
}
