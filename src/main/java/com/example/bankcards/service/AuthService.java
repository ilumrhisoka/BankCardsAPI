package com.example.bankcards.service;

import com.example.bankcards.entity.User;
import com.example.bankcards.entity.enums.Role;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public String login(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsername(username);

        if(userOptional.isPresent()) {
            User user = userOptional.get();
            if(passwordEncoder.matches(password, user.getPassword())) {
                return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
            }
        }
        return null;
    }

    public User register(String username,String email, String password) {
        if(userRepository.findByUsername(username).isPresent()) {
            return null;
        }

        String encodedPassword = passwordEncoder.encode(password);

        User newUser = new User(username, email, encodedPassword, Role.valueOf("ROLE_USER"));
        return userRepository.save(newUser);
    }
}
