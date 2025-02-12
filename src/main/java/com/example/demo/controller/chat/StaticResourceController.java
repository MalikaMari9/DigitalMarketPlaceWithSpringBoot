package com.example.demo.controller.chat;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/images")
public class StaticResourceController {

    @GetMapping("/{filename:.+}")
    public Resource getImage(@PathVariable String filename) {
        return new ClassPathResource("static/images/" + filename);
    }
}
