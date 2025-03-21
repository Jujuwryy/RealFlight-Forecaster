package com.george.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Hidden;
import reactor.core.publisher.Mono;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.reactive.ServerHttpResponse;

@RestController
public class RedirectController {
    private static final Logger logger = LoggerFactory.getLogger(RedirectController.class);

    @Hidden
    @GetMapping("/")
    public Mono<Void> redirectToSwagger(ServerWebExchange exchange) {
        logger.debug("Redirecting from / to /flights-docs");
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(org.springframework.http.HttpStatus.FOUND); // 302 Redirect
        response.getHeaders().setLocation(java.net.URI.create("/flights-docs"));
        return response.setComplete();
    }
}