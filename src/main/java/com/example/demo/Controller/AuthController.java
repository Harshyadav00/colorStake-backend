package com.example.demo.Controller;

import com.example.demo.Entity.Dto.LoginResponse;
import com.example.demo.Entity.Dto.RefreshTokenRequest;
import com.example.demo.Entity.Dto.RegisterRequest;
import com.example.demo.Entity.Dto.TokenResponse;
import com.example.demo.Entity.Entity.User;
import com.example.demo.Exceptions.EmailAlreadyExistsException;
import com.example.demo.Exceptions.InvalidPasswordException;
import com.example.demo.Exceptions.UserNotFoundException;
import com.example.demo.Service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {


    @Autowired
    private AuthService authService;


    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            TokenResponse res = authService.refresh(request);
            return ResponseEntity.ok(res);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired refresh token");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest user) {
        try {
            authService.register(user);
            return ResponseEntity.ok("User registered successfully");
        } catch (EmailAlreadyExistsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        try {
            LoginResponse response = authService.login(user);
            return ResponseEntity.ok(response);
        } catch (UserNotFoundException | InvalidPasswordException e ) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) throws Exception {
        try {

        authService.initiatePasswordReset(email);
        return ResponseEntity.ok("Password reset link has been sent to your email.");
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String token, @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
        return ResponseEntity.ok("Password has been successfully reset.");
    }


}
