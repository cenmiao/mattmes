package com.matt.mes;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordVerifier {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "Admin@123";
        String encodedPassword = "$2a$10$n1yV5fAnUd3auvN/C/bK7ufoMxy2aEt3uP6wC0EnnpR6tS8VqzdBW";
        boolean matches = encoder.matches(rawPassword, encodedPassword);
        System.out.println("Raw: " + rawPassword);
        System.out.println("Encoded: " + encodedPassword);
        System.out.println("Matches: " + matches);
    }
}
