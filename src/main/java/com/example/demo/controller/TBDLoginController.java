package com.example.demo.controller;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.demo.entity.Item;
import com.example.demo.entity.Seller;
import com.example.demo.entity.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.SellerRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.ViewRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class TBDLoginController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private ViewRepository viewRepository;
	@Autowired
	private SellerRepository sellerRepository;

	@GetMapping("/loginPage")
	public String goLogin() {
		return "TBDLogin";
	}

	// Seller Dashboard
	@GetMapping("/sellerDashboard")
	public String sellerDashboard(HttpSession session, Model model) {
		User seller = (User) session.getAttribute("user");
		if (seller == null) {
			return "redirect:/loginPage";
		}

		// ✅ Fetch Total Orders for the seller
		Long totalOrders = orderRepository.countBySeller(seller);

		// ✅ Fetch Total Sales (sum of all orders' price * quantity)
		Double totalSales = orderRepository.sumTotalSalesBySeller(seller);
		if (totalSales == null) {
			totalSales = 0.0;
		}

		// ✅ Fetch Total Visits (number of times items from this seller were viewed)
		Long totalVisits = viewRepository.countViewsBySeller(seller);
		if (totalVisits == null) {
			totalVisits = 0L;
		}

		model.addAttribute("totalOrders", totalOrders);
		model.addAttribute("totalSales", totalSales);
		model.addAttribute("totalVisits", totalVisits);

		return "sellerDashboard";
	}

	@GetMapping("/sellerDashboard/data")
	@ResponseBody
	public List<Map<String, Object>> getSellerDashboardData(HttpSession session) {
		User seller = (User) session.getAttribute("user");
		if (seller == null) {
			return Collections.emptyList();
		}

		List<Map<String, Object>> monthlyData = new ArrayList<>();

		for (int i = 1; i <= 12; i++) {
			Map<String, Object> monthlyStats = new HashMap<>();

			// ✅ Get Monthly Sales
			Double monthlySales = orderRepository.getMonthlySales(seller, i);
			if (monthlySales == null) {
				monthlySales = 0.0;
			}

			// ✅ Get Monthly Views
			Long monthlyViews = viewRepository.getMonthlyViews(seller, i);
			if (monthlyViews == null) {
				monthlyViews = 0L;
			}

			// ✅ Add Data to List
			monthlyStats.put("month", YearMonth.of(LocalDate.now().getYear(), i).getMonth().toString());
			monthlyStats.put("sales", monthlySales);
			monthlyStats.put("views", monthlyViews);
			monthlyData.add(monthlyStats);
		}

		return monthlyData;
	}

	@GetMapping("/sellerDashboard/topProducts")
	@ResponseBody
	public Map<String, Object> getTopProducts(HttpSession session) {
		User sellerUser = (User) session.getAttribute("user");
		if (sellerUser == null) {
			return Collections.singletonMap("error", "User not logged in.");
		}

		Seller seller = sellerRepository.findByUser(sellerUser);
		if (seller == null) {
			return Collections.singletonMap("error", "Seller profile not found.");
		}

		Map<String, Object> response = new HashMap<>();
		List<Map<String, Object>> productList = new ArrayList<>();

		if ("B2C".equals(seller.getBusinessType())) {
			response.put("type", "B2C");

			List<Object[]> topProducts = orderRepository.findTopSellingProducts(sellerUser);
			for (Object[] product : topProducts) {
				Item item = (Item) product[0];
				Map<String, Object> productData = new HashMap<>();
				productData.put("itemID", item.getItemID());
				productData.put("itemName", item.getItemName());
				productData.put("thumbnail", item.getThumbnail());
				productData.put("totalSales", product[1]);
				productData.put("stock", item.getQuality());
				productList.add(productData);
			}
		} else {
			response.put("type", "C2C");

			List<Object[]> recentSales = orderRepository.findRecentlySoldProducts(sellerUser);
			for (Object[] product : recentSales) {
				Item item = (Item) product[0];
				Map<String, Object> productData = new HashMap<>();
				productData.put("itemID", item.getItemID());
				productData.put("itemName", item.getItemName());
				productData.put("thumbnail", item.getThumbnail());
				productData.put("soldDate", product[1]);
				productData.put("quantity", product[2]);
				productList.add(productData);
			}
		}

		response.put("products", productList);
		return response;
	}

	@PostMapping("/loginPage")
	public String login(@RequestParam String username, HttpSession session) {
		if (username.equals("ADMIN")) {
			session.setAttribute("admin", true);
			return "redirect:/admin/viewDashboard";
		}

		// Fetch user from database by username
		Optional<User> optionalUser = userRepository.findByUsername(username);

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			session.setAttribute("user", user); // Store user in session

			session.setAttribute("username", user.getUsername());
			session.setAttribute("user_id", user.getUserID());
			System.out.println("User's role is " + user.getRole());
			System.out.println("Session ID is " + session.getId());

			// Redirect based on role
			if ("BUYER".equals(user.getRole())) { // Corrected string comparison
				return "redirect:/home";
			} else {
				return "redirect:/sellerDashboard";
			}
		} else {
			// Redirect back to login with an error
			return "redirect:/loginPage?error=User not found";
		}
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate(); // Clear session
		return "redirect:/home";
	}

	// API endpoint to fetch users (for your fetch call)
	@GetMapping("/api/users")
	@ResponseBody
	public ResponseEntity<?> getUsers(HttpSession session) {
		String loggedInUsername = (String) session.getAttribute("username");
		System.out.println("Session ID: " + session.getId());
		System.out.println("Session username: " + loggedInUsername);

		if (loggedInUsername == null) {
			return ResponseEntity.status(401).body(Map.of("error", "User not logged in."));
		}

		List<Map<String, Object>> users = userRepository.findAll().stream()
				.filter(user -> !user.getUsername().equals(loggedInUsername)).map(user -> {
					Map<String, Object> userMap = new HashMap<>();
					userMap.put("user_id", user.getUserID());
					userMap.put("username", user.getUsername());
					userMap.put("email", user.getEmail());
					String profile = user.getProfilePath();
					if (profile == null || profile.isEmpty()) {
						profile = "profiledefault.png"; // Adjust this default file name/path as needed.
					}
					userMap.put("profile", profile);
					return userMap;
				}).collect(Collectors.toList());

		return ResponseEntity.ok(users);
	}

	// API endpoint for authentication login (for fetch calls)
	@PostMapping("/api/auth/login")
	@ResponseBody
	public ResponseEntity<?> apiLogin(@RequestBody Map<String, String> loginData, HttpSession session) {
		// Ensure loginData contains the username and password
		String username = loginData.get("username");
		String password = loginData.get("password");

		if (username == null || password == null) {
			return ResponseEntity.badRequest().body(Map.of("error", "Username or password cannot be null"));
		}

		// Find user by username
		Optional<User> optionalUser = userRepository.findByUsername(username);

		// Validate user and password
		if (optionalUser.isPresent()) {
			User foundUser = optionalUser.get();

			// Ensure that the found user has a valid password
			if (foundUser.getPassword() != null && foundUser.getPassword().equals(password)) {
				session.setAttribute("username", foundUser.getUsername());
				session.setAttribute("user_id", foundUser.getUserID());
				String profile = foundUser.getProfilePath();
				if (profile == null || profile.isEmpty()) {
					profile = "profiledefault.png";
				}

				return ResponseEntity.ok(Map.of("message", "Login successful", "username", foundUser.getUsername(),
						"user_id", foundUser.getUserID(), "profile", profile, "role", foundUser.getRole()));
			} else {
				return ResponseEntity.status(401).body(Map.of("error", "Invalid username or password"));
			}
		} else {
			return ResponseEntity.status(401).body(Map.of("error", "User not found"));
		}
	}

}
