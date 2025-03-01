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
@RequestMapping("/uploads")
public class ImageController {

	private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

	@GetMapping("/{filename:.+}")
	public Resource getImage(@PathVariable String filename) {
		try {
			Path filePath = Paths.get(UPLOAD_DIR).resolve(filename).normalize();
			return new UrlResource(filePath.toUri());
		} catch (MalformedURLException e) {
			throw new RuntimeException("Image not found: " + filename, e);
		}
	}
}
