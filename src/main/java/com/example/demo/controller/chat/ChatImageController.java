package com.example.demo.controller.chat;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/Image/Chat")
public class ChatImageController {

	private static final String IMAGE_DIR = "src/main/resources/static/Image/Chat/";

	@GetMapping("/{filename:.+}")
	public Resource getImage(@PathVariable String filename) {
		try {
			Path filePath = Paths.get(IMAGE_DIR).resolve(filename).normalize();
			return new UrlResource(filePath.toUri());
		} catch (MalformedURLException e) {
			throw new RuntimeException("Image not found: " + filename, e);
		}
	}
}
