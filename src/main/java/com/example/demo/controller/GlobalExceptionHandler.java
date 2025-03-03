package com.example.demo.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MissingServletRequestParameterException.class)
	public String handleMissingParamException(MissingServletRequestParameterException ex, Model model,
			HttpServletRequest request) {

		String sellerID = request.getParameter("sellerID"); // ✅ Get sellerID from request
		model.addAttribute("errorMessage", ex.getMessage()); // ✅ Add error message
		model.addAttribute("sellerID", sellerID); // ✅ Pass sellerID to error page

		return "errorPage"; // ✅ Redirect to error page
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public String handleNotFoundError(Model model, HttpServletRequest request) {
		String sellerID = request.getParameter("sellerID"); // ✅ Get sellerID
		model.addAttribute("errorMessage", "Page Not Found");
		model.addAttribute("sellerID", sellerID);
		return "errorPage"; // ✅ Redirect to error page
	}
}
