package com.example.bankcards.controller.auth;

import com.example.bankcards.dto.auth.LoginRequest;
import com.example.bankcards.security.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        String username = loginRequest.getUsername();
        String password = loginRequest.getPassword();
        String role = "admin".equals(username) ? "ROLE_ADMIN" : "ROLE_USER";
        if (("admin".equals(username) && "password".equals(password)) ||
                ("user".equals(username) && "password".equals(password))) {
            String token = JwtUtil.generateToken(username, role);
            return ResponseEntity.ok().body(token);
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}