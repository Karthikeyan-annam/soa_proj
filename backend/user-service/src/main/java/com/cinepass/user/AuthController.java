package com.cinepass.user;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwtService) {
        this.users = users;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        String email = required(body, "email").toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        UserAccount user = new UserAccount();
        user.setFullName(required(body, "fullName"));
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(required(body, "password")));
        users.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(authPayload(user));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        UserAccount user = users.findByEmail(required(body, "email").toLowerCase())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!encoder.matches(required(body, "password"), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return authPayload(user);
    }

    @GetMapping("/me")
    public Map<String, Object> me(@RequestHeader("X-User-Id") Long userId) {
        UserAccount user = users.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return Map.of("id", user.getId(), "fullName", user.getFullName(), "email", user.getEmail());
    }

    private Map<String, Object> authPayload(UserAccount user) {
        return Map.of(
                "token", jwtService.issue(user),
                "user", Map.of("id", user.getId(), "fullName", user.getFullName(), "email", user.getEmail())
        );
    }

    private String required(Map<String, String> body, String key) {
        String value = body.get(key);
        if (value == null || value.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, key + " is required");
        }
        return value.trim();
    }
}
