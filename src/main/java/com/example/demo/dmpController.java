package com.example.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class dmpController {
	@GetMapping("/viewItem")
	public String viewItem() {
		return "viewSale";
		
	}
	@GetMapping("/viewProfile")
	public String viewProfile() {
		return "viewprofile";
		
	}
	@GetMapping("/test")
	public String test() {
		return "test";
		
	}
	
	//Site that has header footers
	
	@GetMapping("/addressBook")
	public String addressBook() {
		return "addressbook";
		
	}
	
	@GetMapping("/viewBuyer")
	public String viewBuyer() {
		return "buyer";
		
	}
	
	@GetMapping("/viewSeller")
	public String viewSeller() {
		return "viewprofile";
		
	}
	
	@GetMapping("/wishList")
	public String wishList() {
		return "wish";
		
	}
	
	@GetMapping("/editProfile")
	public String editProfile() {
		return "editprofile";
	}
	
	@GetMapping("/changePassword")
	public String changePassword() {
		return "changepassword";
	}
	
	@GetMapping("/orderHistory")
	public String orderHistory() {
		return "orderhistory";
	}
	
	
}
