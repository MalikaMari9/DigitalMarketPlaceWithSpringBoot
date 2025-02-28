package com.example.demo.controller;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.demo.entity.Address;
import com.example.demo.entity.User;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller

public class UserController {

	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AddressRepository addressRepository;

	// Show Registration Form
	@GetMapping("/register")
	public String showRegistrationForm(Model model) {
		model.addAttribute("user", new User());
		return "Registeration";
	}

	// Handle Registration
	@PostMapping("/register")
	public String registerUser(@ModelAttribute User user, Model model) throws NoSuchAlgorithmException {
		// Check if username exists
		if (userRepository.findByUsername(user.getUsername()).isPresent()) {
			model.addAttribute("usernameError", "Username is already taken");
			return "Registeration";
		}

		// Hash password using SHA-256
		user.setPassword(hashPassword(user.getPassword()));

		// Save user
		userRepository.save(user);

		return "redirect:login";
	}

	// Show Login Form
	@GetMapping("/login")
	public String showLoginForm() {
		return "login";
	}

	@PostMapping("/login")
	public String loginUser(@RequestParam String username, @RequestParam String password, HttpServletRequest request,
			Model model) throws NoSuchAlgorithmException {
		if (username.equals("ADMIN")) {
			return "redirect:/admin/viewDashboard";
		}
		Optional<User> userOptional = userRepository.findByUsername(username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();

			// Compare stored hash with newly hashed input password
			if (user.getPassword().equals(hashPassword(password))) {
				HttpSession session = request.getSession();
				session.setAttribute("user", user);
				return "redirect:/home";
			}
		}

		model.addAttribute("loginError", "Invalid username or password");
		return "login";
	}

	@GetMapping("/addressbook")
	public String showAddressBook(HttpSession session, Model model) {
		// ✅ Retrieve User object from session
		User user = (User) session.getAttribute("user");
		if (user == null) {
			System.out.println("❌ No user in session! Redirecting to login.");
			return "redirect:/login";
		}

		Long userID = user.getUserID();
		System.out.println("✅ Logged-in User ID: " + userID);

		// ✅ Fetch addresses for the logged-in user
		List<Address> addresses = addressRepository.findByUserUserID(userID);
		System.out.println("📌 Retrieved " + addresses.size() + " addresses for user ID: " + userID);

		if (addresses.isEmpty()) {
			System.out.println("⚠️ No addresses found for user ID: " + userID);
		} else {
			for (Address address : addresses) {
				System.out.println("📍 Address: " + address.getCustName() + ", " + address.getAddres());
			}
		}

		// ✅ Add data to model
		model.addAttribute("addresses", addresses);
		return "addressbook"; // ✅ Thymeleaf template
	}

	// ✅ Show Address Form
	@GetMapping("/address-form/{userID}")
	public String showAddressForm(@PathVariable("userID") Long userID, HttpSession session, Model model) {
		// ✅ Retrieve User object from session
		User user = (User) session.getAttribute("user");

		if (user == null && userID == null) {
			return "redirect:/login"; // Redirect if no user is found
		}

		// ✅ Use userID from session if null
		Long actualUserID = (userID != null) ? userID : user.getUserID();

		// ✅ Get the actual user from the database
		Optional<User> userOptional = userRepository.findById(actualUserID);
		if (userOptional.isEmpty()) {
			return "redirect:/login";
		}

		User registeredUser = userOptional.get();

		// ✅ Store the user object in session (if registering)
		session.setAttribute("user", registeredUser);

		// ✅ Add empty address object for form binding
		model.addAttribute("address", new Address());

		return "editaddress"; // ✅ Load address entry page
	}

	// ✅ Save Address
	@PostMapping("/save-address/{userID}")
	public String saveAddress(@PathVariable("userID") Long userID, @RequestParam("custName") String custName,
			@RequestParam("postalCode") String postalCode, @RequestParam("phone") String phone,
			@RequestParam("township") String township, @RequestParam("building") String building,
			@RequestParam("city") String city, @RequestParam("addres") String addressDetail,
			@RequestParam("delinote") String deliveryNote, HttpSession session) {

		// ✅ Retrieve User object from session
		User user = (User) session.getAttribute("user");

		if (user == null && userID == null) {
			System.out.println("❌ No user found in session! Redirecting to login.");
			return "redirect:/login";
		}

		// ✅ Use userID from session if null
		Long actualUserID = (userID != null) ? userID : user.getUserID();

		// ✅ Fetch the user from the database
		Optional<User> userOptional = userRepository.findById(actualUserID);
		if (userOptional.isEmpty()) {
			return "redirect:/login";
		}
		User registeredUser = userOptional.get();

		// ✅ Create and save the new Address
		Address address = new Address();
		address.setCustName(custName);
		address.setPostalCode(postalCode);
		address.setPhone(phone);
		address.setTownship(township);
		address.setBuilding(building);
		address.setCity(city);
		address.setAddres(addressDetail);
		address.setDelinote(deliveryNote);
		address.setUser(registeredUser); // Assign the user

		addressRepository.save(address);

		System.out.println("✅ Address saved successfully!");

		// ✅ Redirect to the address book
		return "redirect:/addressbook";
	}

	// ✅ Delete Address
	@GetMapping("/delete-address/{id}")
	public String deleteAddress(@PathVariable("id") Long addressID) {
		Optional<Address> addressOptional = addressRepository.findById(addressID);

		if (addressOptional.isPresent()) {
			addressRepository.deleteById(addressID);
			return "redirect:/addressbook"; // ✅ Redirect to address book after deleting
		}

		return "redirect:/addressbook?error=AddressNotFound"; // If not found
	}

	// ✅ Show Edit Address Form
	@GetMapping("/edit-address/{id}")
	public String showEditAddressForm(@PathVariable("id") Long addressID, Model model) {
		// ✅ Find the address by ID
		Optional<Address> addressOptional = addressRepository.findById(addressID);
		if (addressOptional.isEmpty()) {
			return "redirect:/profile?error=AddressNotFound";
		}

		Address address = addressOptional.get();

		// ✅ Fetch available options for dropdowns (if needed)
		List<String> townships = Arrays.asList("Yangon", "Mandalay");
		List<String> cities = Arrays.asList("Yangon", "Mandalay");

		// ✅ Pass data to the model
		model.addAttribute("address", address);
		model.addAttribute("townships", townships);
		model.addAttribute("cities", cities);

		return "UpdateAddress"; // ✅ Must match 'editaddress.html'
	}

	// ✅ Update Address
	@PostMapping("/update-address/{id}")
	public String updateAddress(@PathVariable("id") Long addressID, @RequestParam("custName") String custName,
			@RequestParam("postalCode") String postalCode, @RequestParam("phone") String phone,
			@RequestParam("township") String township, @RequestParam("building") String building,
			@RequestParam("city") String city, @RequestParam("address") String addressDetail,
			@RequestParam("deliveryNote") String deliveryNote) {

		// ✅ Find the existing address
		Optional<Address> addressOptional = addressRepository.findById(addressID);
		if (addressOptional.isEmpty()) {
			return "redirect:/profile?error=AddressNotFound";
		}

		Address existingAddress = addressOptional.get();

		// ✅ Update fields
		existingAddress.setCustName(custName);
		existingAddress.setPostalCode(postalCode);
		existingAddress.setPhone(phone);
		existingAddress.setTownship(township);
		existingAddress.setBuilding(building);
		existingAddress.setCity(city);
		existingAddress.setAddres(addressDetail);
		existingAddress.setDelinote(deliveryNote);

		// ✅ Save updated address
		addressRepository.save(existingAddress);

		return "redirect:/addressbook";
	}

	// Notification

	// Method to hash password using SHA-256
	private String hashPassword(String password) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] hashedBytes = md.digest(password.getBytes());

		// Convert bytes to hex format
		StringBuilder hexString = new StringBuilder();
		for (byte b : hashedBytes) {
			String hex = Integer.toHexString(0xff & b);
			if (hex.length() == 1)
				hexString.append('0');
			hexString.append(hex);
		}
		return hexString.toString();
	}
}
