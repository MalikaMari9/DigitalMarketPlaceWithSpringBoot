package com.example.demo.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.entity.Address;
import com.example.demo.entity.PasswordReset;
import com.example.demo.entity.User;
import com.example.demo.repository.AddressRepository;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.PasswordResetRepository;
import com.example.demo.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller

public class UserController {

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private PasswordResetRepository passwordResetRepository;
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private AddressRepository addressRepository;
	@Autowired
	private NotificationRepository notificationRepo;

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
			Model model, HttpSession session) throws NoSuchAlgorithmException {

		if ("ADMIN".equalsIgnoreCase(username) && "Admin12345".equals(password)) {
			session.setAttribute("admin", true);
			return "redirect:/admin/viewDashboard";
		}

		// ✅ Fetch user from the database
		Optional<User> optionalUser = userRepository.findByUsername(username);

		if (optionalUser.isPresent()) {
			User user = optionalUser.get();
			System.out.println("User's role is: " + user.getRole()); // Debugging role
			System.out.println("Session ID is: " + session.getId());

			// ✅ Verify Password
			if (!user.getPassword().equals(hashPassword(password))) {
				model.addAttribute("loginError", "Invalid password");
				return "login"; // Redirect back to login page with error
			}

			// ✅ Store user details in session
			session.setAttribute("user", user); // Full user object
			session.setAttribute("username", user.getUsername());
			session.setAttribute("user_id", user.getUserID());

			// ✅ Redirect based on Role
			if ("BUYER".equalsIgnoreCase(user.getRole())) {
				return "redirect:/home"; // Redirect Buyers to home page
			} else if ("SELLER".equalsIgnoreCase(user.getRole())) {
				return "redirect:/sellerDashboard"; // Redirect Sellers to their dashboard
			}
		} else {
			model.addAttribute("loginError", "Invalid username");
		}

		return "login"; // Redirect back to login page if authentication fails
	}

	@GetMapping("/addressbook")
	public String showAddressBook(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");
		if (user == null) {
			return "redirect:/login";
		}

		Long userID = user.getUserID();
		List<Address> addresses = addressRepository.findByUserUserIDAndIsDeletedFalse(userID); // ✅ Fetch only active
																								// addresses

		model.addAttribute("addresses", addresses);
		return "addressbook";
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

		List<String> cities = addressRepository.findAllCityNames();
		model.addAttribute("cities", cities);
		// ✅ Add empty address object for form binding
		model.addAttribute("address", new Address());

		return "addAddress"; // ✅ Load address entry page
	}

	@PostMapping("/set-main")
	public ResponseEntity<String> setMainAddress(@RequestParam Long addressID, HttpSession session) {
		// ✅ Get user from session
		User user = (User) session.getAttribute("user");
		if (user == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("You must be logged in.");
		}

		// ✅ Unset previous main address (if any)
		Address currentMain = addressRepository.findMainAddressByUser(user.getUserID());
		if (currentMain != null) {
			currentMain.setIsMainAddress(false);
			addressRepository.save(currentMain);
		}

		// ✅ Set new main address
		Address newMain = addressRepository.findById(addressID)
				.orElseThrow(() -> new RuntimeException("Address not found"));

		// ✅ Ensure the address belongs to the user
		if (!newMain.getUser().getUserID().equals(user.getUserID())) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Unauthorized action.");
		}

		newMain.setIsMainAddress(true);
		addressRepository.save(newMain);

		return ResponseEntity.ok("Main address updated successfully.");
	}

	@GetMapping("/townships")
	@ResponseBody
	public List<String> getTownships(@RequestParam("cityName") String cityName) {
		System.out.println("Fetching townships for city: " + cityName); // ✅ Debugging log

		List<String> townships = addressRepository.findTownshipNamesByCity(cityName);
		System.out.println("Townships found: " + townships); // ✅ Debugging log

		return townships;
	}

	@PostMapping("/save-address/{userID}")
	public String saveAddress(@PathVariable("userID") Long userID, @RequestParam("custName") String custName,
			@RequestParam("addressName") String addName, @RequestParam("postalCode") String postalCode,
			@RequestParam("phone") String phone, @RequestParam("township") String township,
			@RequestParam("building") String building, @RequestParam("city") String city,
			@RequestParam("addres") String addressDetail, @RequestParam("delinote") String deliveryNote,
			HttpSession session) {

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

		// ✅ Check if the user already has a main address
		boolean hasMainAddress = addressRepository.existsByUserAndIsMainAddressTrue(registeredUser);

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
		address.setAddressName(addName);
		address.setUser(registeredUser);

		// ✅ If this is the first address, set it as main
		if (!hasMainAddress) {
			address.setIsMainAddress(true);
		} else {
			address.setIsMainAddress(false);
		}

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

	@PostMapping("/delete-address")
	@ResponseBody
	public String deleteAddress(@RequestParam Long addressID, HttpSession session) {
		Address address = addressRepository.findById(addressID).orElse(null);
		if (address != null) {
			User user = (User) session.getAttribute("user");

			// ✅ If the user is a seller and the address is the main address, prevent
			// deletion
			if (user != null && "SELLER".equals(user.getRole()) && address.getIsMainAddress()) {
				return "You cannot delete your main address.";
			}

			address.setDeleted(true); // ✅ Soft delete (set isDeleted to true)
			addressRepository.save(address);
			return "Address deleted successfully.";
		}
		return "Address not found.";
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

		// ✅ Fetch all city names
		List<String> cities = addressRepository.findAllCityNames();

		// ✅ Add existing address & city list to the model
		model.addAttribute("address", address);
		model.addAttribute("cities", cities);

		return "UpdateAddress"; // ✅ Matches `UpdateAddress.html`
	}

	// ✅ Update Address
	@PostMapping("/update-address/{id}")
	public String updateAddress(@PathVariable("id") Long addressID, @RequestParam("custName") String custName,
			@RequestParam("addressName") String addName, @RequestParam("postalCode") String postalCode,
			@RequestParam("phone") String phone, @RequestParam("township") String township,
			@RequestParam("building") String building, @RequestParam("city") String city,
			@RequestParam("addres") String addressDetail, @RequestParam("delinote") String deliveryNote) {

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
		existingAddress.setAddressName(addName);

		// ✅ Save updated address
		addressRepository.save(existingAddress);

		return "redirect:/addressbook";
	}

	// EditProfile
	@GetMapping("/editProfile")
	public String showEditProfileForm(HttpSession session, Model model) {
		// ✅ Retrieve User object correctly from session
		Object sessionUser = session.getAttribute("user");

		if (!(sessionUser instanceof User)) {
			return "redirect:/login"; // Redirect if session expired
		}

		User user = (User) sessionUser; // ✅ Now correctly retrieving User object

		// ✅ Ensure the user exists in the database
		Optional<User> userOptional = userRepository.findById(user.getUserID());
		if (userOptional.isEmpty()) {
			return "redirect:/login?error=UserNotFound";
		}
		user = userOptional.get();

		// ✅ Add user to model for Thymeleaf binding
		model.addAttribute("user", user);
		return "editprofile"; // ✅ Thymeleaf template name
	}

	@PostMapping("/editProfile")
	public String updateProfile(@ModelAttribute User updatedUser,
			@RequestPart(value = "profilePicture", required = false) MultipartFile profilePicture, HttpSession session,
			Model model) throws IOException {

		// ✅ Validate session
		User user = (User) session.getAttribute("user");
		if (user == null)
			return "redirect:/login";

		Optional<User> userOptional = userRepository.findById(user.getUserID());
		if (userOptional.isEmpty())
			return "redirect:/login?error=UserNotFound";

		user = userOptional.get();

		// ✅ Validate required fields
		if (updatedUser.getUsername() == null || updatedUser.getUsername().isEmpty()) {
			model.addAttribute("error", "Username cannot be empty");
			return "editprofile";
		}
		if (updatedUser.getEmail() == null || updatedUser.getEmail().isEmpty()) {
			model.addAttribute("error", "Email cannot be empty");
			return "editprofile";
		}

		// ✅ Update basic fields
		user.setUsername(updatedUser.getUsername());
		user.setEmail(updatedUser.getEmail());
		if (updatedUser.getPhone() != null)
			user.setPhone(updatedUser.getPhone());
		if (updatedUser.getDob() != null)
			user.setDob(updatedUser.getDob());
		if (updatedUser.getGender() != null)
			user.setGender(updatedUser.getGender());
		if (updatedUser.getBio() != null)
			user.setBio(updatedUser.getBio());

		// ✅ Handle profile picture upload
		if (profilePicture != null && !profilePicture.isEmpty()) {
			// ✅ Validate file type
			if (!List.of("image/jpeg", "image/png", "image/jpg").contains(profilePicture.getContentType())) {
				model.addAttribute("error", "Invalid file format. Only JPG, PNG allowed.");
				return "editprofile";
			}

			// ✅ Validate file size (max 2MB)
			if (profilePicture.getSize() > 2 * 1024 * 1024) {
				model.addAttribute("error", "File size exceeds 2MB.");
				return "editprofile";
			}

			// ✅ Define upload path
			String uploadDir = "src/main/resources/static/Image/profile/";
			File dir = new File(uploadDir);
			if (!dir.exists())
				dir.mkdirs();

			// ✅ Generate unique filename
			String cleanFileName = profilePicture.getOriginalFilename().replaceAll("\\s+", "_");
			String fileName = user.getUserID() + "_" + System.currentTimeMillis() + "_" + cleanFileName;
			Path filePath = Path.of(uploadDir + fileName);

			// ✅ Save the file
			Files.copy(profilePicture.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

			// ✅ Store only the filename in the database
			user.setProfilePath(fileName);
		}

		// ✅ Save updated user
		userRepository.save(user);

		// ✅ **Update session with new user data**
		session.setAttribute("user", user); // **Fixes delayed profile update issue**

		return "redirect:/home";
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

	@GetMapping("/gmail")
	public String gmail(HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user == null)
			return "redirect:/login";
		return "gmail";
	}

	@GetMapping("/gmailForgetPassword")
	public String gmailPassword(HttpSession session) {

		return "forgetPasswordGmail";
	}

	@PostMapping("/gmailForgetPassword")
	public String handleForgetPasswordGmail(@RequestParam String email, @RequestParam String username, Model model,
			HttpSession session) {

		Optional<User> userOptional = userRepository.findByEmailandUsername(email, username);

		if (userOptional.isPresent()) {
			String token = generateResetToken();
			LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

			// ✅ Debugging: Print Generated Token
			System.out.println("✅ Generated Token: " + token);

			PasswordReset passwordReset = new PasswordReset(userOptional.get(), token, expiryDate);
			passwordResetRepository.save(passwordReset);

			// ✅ Debugging: Check if token is saved
			Optional<PasswordReset> checkSaved = passwordResetRepository.findByToken(token);
			if (checkSaved.isPresent()) {
				System.out.println("✅ Token successfully saved in the database!");
			} else {
				System.out.println("❌ Token not found in database after save!");
			}

			sendPasswordResetEmail(email, token);
			System.out.println("📧 Email sent to: " + email);
			return "redirect:/reset-codeForgetPassword?email=" + email + "&username=" + username;
		} else {
			System.out.print("No user opt");
		}

		model.addAttribute("message",
				"If an account exists with this email, you will receive password reset instructions.");
		return "gmailForgetPassword";
	}

	@PostMapping("/gmail")
	public String handleForgetPassword(@RequestParam String email, Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user == null)
			return "redirect:/login";

		String username = user.getUsername();

		System.out.println("This function should work");
		Optional<User> userOptional = userRepository.findByEmailandUsername(email, username);

		if (userOptional.isPresent()) {
			String token = generateResetToken();
			LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

			// ✅ Debugging: Print Generated Token
			System.out.println("✅ Generated Token: " + token);

			PasswordReset passwordReset = new PasswordReset(userOptional.get(), token, expiryDate);
			passwordResetRepository.save(passwordReset);

			// ✅ Debugging: Check if token is saved
			Optional<PasswordReset> checkSaved = passwordResetRepository.findByToken(token);
			if (checkSaved.isPresent()) {
				System.out.println("✅ Token successfully saved in the database!");
			} else {
				System.out.println("❌ Token not found in database after save!");
			}

			sendPasswordResetEmail(email, token);
			System.out.println("📧 Email sent to: " + email);
			return "redirect:/reset-code?email=" + email;
		} else {
			System.out.print("No user opt");
		}

		model.addAttribute("message",
				"If an account exists with this email, you will receive password reset instructions.");
		return "gmail";
	}

	@GetMapping("/reset-code")
	public String showResetCodeForm(@RequestParam String email, Model model) {
		model.addAttribute("email", email);

		return "resetcode";
	}

	@GetMapping("/reset-codeForgetPassword")
	public String showResetCodeFormFP(@RequestParam String email, @RequestParam String username, Model model) {
		model.addAttribute("email", email);
		model.addAttribute("username", username);
		return "resetcodeForgetPassword";
	}

	@PostMapping("/verify-reset-codeForgetPassword")
	public String verifyResetCodeFP(@RequestParam String email, @RequestParam String username,
			@RequestParam String token, Model model, HttpSession session) {

		Optional<User> userOptional = userRepository.findByEmailandUsername(email, username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			Long userId = userOptional.get().getUserID();
			Optional<PasswordReset> resetOptional = passwordResetRepository.findByTokenAndUsedFalse(token);

			if (resetOptional.isPresent()) {
				PasswordReset reset = resetOptional.get();
				if (reset.getExpiryDate().isAfter(LocalDateTime.now()) && reset.getUser().getUserID().equals(userId)) {
					reset.setUsed(true);
					passwordResetRepository.save(reset);
					return "redirect:/reset-passwordForgetPassword?email=" + email + "&username=" + username + "&token="
							+ token;
				}
			}
		}

		model.addAttribute("error", "Invalid or expired code");
		model.addAttribute("email", email);
		return "resetcodeForgetPassword";
	}

	@PostMapping("/verify-reset-code")
	public String verifyResetCode(@RequestParam String email, @RequestParam String token, Model model,
			HttpSession session) {
		User user1 = (User) session.getAttribute("user");
		if (user1 == null) {
			return "redirect:/login";
		}
		String username = user1.getUsername();
		Optional<User> userOptional = userRepository.findByEmailandUsername(email, username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			Long userId = userOptional.get().getUserID();
			Optional<PasswordReset> resetOptional = passwordResetRepository.findByTokenAndUsedFalse(token);

			if (resetOptional.isPresent()) {
				PasswordReset reset = resetOptional.get();
				if (reset.getExpiryDate().isAfter(LocalDateTime.now()) && reset.getUser().getUserID().equals(userId)) {
					reset.setUsed(true);
					passwordResetRepository.save(reset);
					return "redirect:/reset-password?email=" + email + "&token=" + token;
				}
			}
		}

		model.addAttribute("error", "Invalid or expired code");
		model.addAttribute("email", email);
		return "resetcode";
	}

	@GetMapping("/reset-password")
	public String showResetPasswordForm(@RequestParam String email, @RequestParam String token, Model model) {
		model.addAttribute("email", email);
		model.addAttribute("token", token);
		return "resetpassword";
	}

	@PostMapping("/reset-password")
	@Transactional
	public String handlePasswordReset(@RequestParam String email, @RequestParam String token, HttpSession session,
			@RequestParam String newPassword, Model model) throws NoSuchAlgorithmException {
		User user1 = (User) session.getAttribute("user");
		if (user1 == null)
			return "redirect:/login";

		String username = user1.getUsername();
		Optional<User> userOptional = userRepository.findByEmailandUsername(email, username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			Optional<PasswordReset> resetOptional = passwordResetRepository.findByToken(token);

			if (resetOptional.isPresent()) {
				PasswordReset reset = resetOptional.get();
				if (reset.getUser().getUserID().equals(user.getUserID())) {
					// Update password
					user.setPassword(hashPassword(newPassword));
					userRepository.save(user);

					return "redirect:/login?resetSuccess=true";
				}
			}
		}

		model.addAttribute("error", "Invalid reset attempt");
		return "resetpassword";
	}

	@GetMapping("/reset-passwordForgetPassword")
	public String showResetPasswordFormFP(@RequestParam String email, @RequestParam String username,
			@RequestParam String token, Model model) {
		model.addAttribute("username", username);
		model.addAttribute("email", email);
		model.addAttribute("token", token);
		return "resetpasswordForgetPassword";
	}

	@PostMapping("/reset-passwordForgetPassword")
	@Transactional
	public String handlePasswordResetFP(@RequestParam String email, @RequestParam String username,
			@RequestParam String token, HttpSession session, @RequestParam String newPassword, Model model)
			throws NoSuchAlgorithmException {

		Optional<User> userOptional = userRepository.findByEmailandUsername(email, username);

		if (userOptional.isPresent()) {
			User user = userOptional.get();
			Optional<PasswordReset> resetOptional = passwordResetRepository.findByToken(token);

			if (resetOptional.isPresent()) {
				PasswordReset reset = resetOptional.get();
				if (reset.getUser().getUserID().equals(user.getUserID())) {
					// Update password
					user.setPassword(hashPassword(newPassword));
					userRepository.save(user);

					return "redirect:/login?resetSuccess=true";
				}
			}
		}

		model.addAttribute("error", "Invalid reset attempt");
		return "resetpasswordForgetPassword";
	}

	private String generateResetToken() {
		return String.valueOf((int) ((Math.random() * 900000) + 100000));
	}

	private void sendPasswordResetEmail(String to, String token) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom("hanlin1646@gmail.com");
		message.setTo(to);
		message.setSubject("Password Reset Request");
		message.setText("Use this code to reset your password: " + token + "\n\n"
				+ "Go to this link to reset: http://localhost:8080/reset-code?email=" + to);

		// ✅ Debugging Log
		System.out.println("📩 Sending password reset email to: " + to);
		System.out.println("🔗 Reset link: http://localhost:8080/reset-code?email=" + to);

		mailSender.send(message);
	}

}
