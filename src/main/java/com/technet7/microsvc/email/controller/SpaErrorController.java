package com.technet7.microsvc.email.controller;

import java.io.IOException;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class SpaErrorController implements ErrorController {

    @RequestMapping("/error")
    public ResponseEntity<Resource> handleError(HttpServletRequest request) throws IOException {
        Object status = request.getAttribute("jakarta.servlet.error.status_code");
        Object uriObj = request.getAttribute("jakarta.servlet.error.request_uri");
        String uri = uriObj == null ? "" : uriObj.toString();

        // If 404 and not an API path and not a static file (no dot), return index.html so the SPA router can handle it.
        if (status != null && status.toString().equals("404")) {
            String lower = uri.toLowerCase();
            if (!lower.startsWith("/api") && !lower.contains(".") && !lower.startsWith("/emails") && !lower.startsWith("/auth") && !lower.startsWith("/admin") && !lower.startsWith("/mindsdb")) {
                Resource index = new ClassPathResource("static/index.html");
                if (index.exists()) {
                    return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(index);
                }
            }
        }

        // Otherwise allow the default error handling (return 404 with default container page)
        return ResponseEntity.status(status == null ? 500 : Integer.parseInt(status.toString())).build();
    }

}
