package com.example.demo.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.CreditCard;
import com.example.demo.entity.User;
import com.example.demo.repository.CreditCardRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class CardController {

	@Autowired
	private CreditCardRepository creditCardRepository;

	@GetMapping("/payment-book")
	public String getPaymentBook(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user"); // ✅ Get User object
		if (user == null)
			return "redirect:/login"; // Redirect if not logged in

		Long userID = user.getUserID(); // ✅ Extract user ID
		List<CreditCard> cards = creditCardRepository.findByUser_UserID(userID);
		model.addAttribute("cards", cards);

		return "paymentBook"; // Load the Thymeleaf template
	}

	@GetMapping("/add-payment")
	public String addPayment(HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user == null)
			return "redirect:/login"; // Redirect if not logged in

		return "card"; // Load creditcard.html
	}

	@PostMapping("/card-details")
	@ResponseBody
	public Map<String, Object> saveCreditCard(@RequestBody Map<String, String> requestData, HttpSession session) {
		Map<String, Object> response = new HashMap<>();

		// ✅ Check if User is Logged In
		User user = (User) session.getAttribute("user");
		if (user == null) {
			response.put("success", false);
			response.put("message", "User not logged in.");
			return response;
		}

		// ✅ Validate Credit Card Information
		String cardOwner = requestData.get("cardOwner");
		String cardNumber = requestData.get("cardNumber");
		String expirationDate = requestData.get("expirationDate");
		String postalCode = requestData.get("postalCode");
		String cardType = requestData.get("cvCode");

		System.out.println("📥 Received Data:");
		System.out.println("🔹 Card Owner: " + cardOwner);
		System.out.println("🔹 Card Number: " + cardNumber);
		System.out.println("🔹 Expiration Date: " + expirationDate);
		System.out.println("🔹 Postal Code: " + postalCode);

		if (cardOwner == null || cardNumber == null || expirationDate == null || postalCode == null) {
			response.put("success", false);
			response.put("message", "Missing credit card details.");
			return response;
		}

		// ✅ Validate Expiration Date
		if (!isValidExpirationDate(expirationDate)) {
			response.put("success", false);
			response.put("message", "❌ Invalid expiration date! Must be at least 1 full month ahead.");
			return response;
		}

		// ✅ Extract Last 4 Digits of the Card
		String lastFourDigits = cardNumber.substring(cardNumber.length() - 4);

		// ✅ Save Credit Card Info
		CreditCard creditCard = new CreditCard(user, lastFourDigits, cardOwner, expirationDate, postalCode, cardType);
		creditCardRepository.save(creditCard);

		response.put("success", true);
		response.put("message", "✅ Credit card saved successfully!");
		return response;
	}

	/**
	 * ✅ Backend Expiration Date Validation (Must be at least 1 month in the future)
	 */
	private boolean isValidExpirationDate(String expirationDate) {
		try {
			System.out.println("🔍 Validating expiration date: " + expirationDate);

			// ✅ Split input "MM/YYYY"
			String[] parts = expirationDate.split("/");
			if (parts.length != 2) {
				System.out.println("❌ Invalid format: MM/YYYY expected.");
				return false;
			}

			int expMonth = Integer.parseInt(parts[0].trim());
			int expYear = Integer.parseInt(parts[1].trim());

			if (expMonth < 1 || expMonth > 12) {
				System.out.println("❌ Invalid month: " + expMonth);
				return false;
			}

			// ✅ Get the current date and minimum valid expiration date
			LocalDate today = LocalDate.now();
			YearMonth currentMonth = YearMonth.of(today.getYear(), today.getMonthValue());
			YearMonth expiryMonth = YearMonth.of(expYear, expMonth); // Use full YYYY

			// ✅ Expiration date must be **at least one month ahead**
			if (!expiryMonth.isAfter(currentMonth.plusMonths(1))) {
				System.out.println("❌ Expiration date is too soon.");
				return false;
			}

			System.out.println("✅ Expiration date is valid.");
			return true;

		} catch (NumberFormatException e) {
			System.out.println("❌ Number format error in expiration date.");
			return false;
		}
	}

	@PostMapping("/delete-card")
	@ResponseBody
	public Map<String, Object> deleteCard(@RequestParam Long cardID, HttpSession session) {
		Map<String, Object> response = new HashMap<>();

		// ✅ Check if User is Logged In
		User user = (User) session.getAttribute("user");
		if (user == null) {
			response.put("success", false);
			response.put("message", "User not logged in.");
			return response;
		}

		// ✅ Fetch the Credit Card from the Database
		Optional<CreditCard> creditCardOptional = creditCardRepository.findById(cardID);
		if (!creditCardOptional.isPresent()) {
			response.put("success", false);
			response.put("message", "Credit card not found.");
			return response;
		}

		CreditCard creditCard = creditCardOptional.get();

		// ✅ Ensure the Logged-in User Owns the Credit Card
		if (!creditCard.getUser().getUserID().equals(user.getUserID())) {
			response.put("success", false);
			response.put("message", "Unauthorized request.");
			return response;
		}

		// ✅ Delete the Credit Card
		creditCardRepository.delete(creditCard);

		response.put("success", true);
		response.put("message", "Credit card deleted successfully.");
		return response;
	}

}
