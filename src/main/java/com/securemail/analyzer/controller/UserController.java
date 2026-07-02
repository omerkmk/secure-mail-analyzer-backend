package com.securemail.analyzer.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @GetMapping("/api/users/me")
    public ResponseEntity<String> me(Authentication authentication) {
        return ResponseEntity.ok("Giriş yapan kullanıcı: " + authentication.getName());
    }
}