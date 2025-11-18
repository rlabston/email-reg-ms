package com.technet7.microsvc.email.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Keep a simple root mapping to serve index.html at the root URL. Complex
 * fallback handling (for deep client-side routes) is implemented in
 * `WebConfig` (a WebMvcConfigurer) to avoid complex path patterns on
 * controller methods which can cause PatternParseException in tests.
 */
@Controller
public class SpaController {

    @GetMapping("/")
    public ResponseEntity<Resource> root() {
        Resource index = new ClassPathResource("static/index.html");
        if (index.exists()) {
            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(index);
        }
        return ResponseEntity.notFound().build();
    }

}
