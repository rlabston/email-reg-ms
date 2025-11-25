package com.technet7.microsvc.gateway;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Controller to serve Angular static files from the gateway.
 * All non-API routes serve index.html for client-side routing.
 */
@Controller
public class StaticContentController {

    @GetMapping(value = {"/", "/login", "/register", "/emails", "/chatbot", "/admin"}, produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public Mono<Resource> index() {
        return Mono.just(new ClassPathResource("static/index.html"));
    }
}
