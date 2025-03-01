package com.example.demo.controller.chat;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/images")
public class StaticResourceController {

	@GetMapping("/{filename:.+}")
	public Resource getImage(@PathVariable String filename) {
		return new ClassPathResource("static/images/" + filename);
	}
}
