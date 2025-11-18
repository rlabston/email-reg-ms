package com.technet7.microsvc.email.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Lightweight forwarding controller to support clients that call the API
 * under the `/api` prefix while server controllers are declared without it.
 *
 * Example: POST /api/emails/login -> forwarded to /emails/login internally.
 */
@Controller
@RequestMapping("/api")
public class ApiForwardController {

    @RequestMapping("/**")
    public String forwardApi(HttpServletRequest request) {
        // Request URI includes contextPath; remove contextPath + "/api" prefix
        String context = request.getContextPath() == null ? "" : request.getContextPath();
        String uri = request.getRequestURI();
        String apiPrefix = context + "/api";
        String forwardPath = uri.startsWith(apiPrefix) ? uri.substring(apiPrefix.length()) : uri;
        if (forwardPath.isEmpty()) {
            forwardPath = "/";
        }
        // Forward internally to the path without /api. This keeps the original HTTP method
        // and request body/query string intact.
        return "forward:" + forwardPath;
    }

}
