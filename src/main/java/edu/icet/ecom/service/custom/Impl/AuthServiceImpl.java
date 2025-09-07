//package edu.icet.ecom.service.custom.Impl;
//
//
//import edu.icet.ecom.Exceptions.InvalidCredentialsException;
//import edu.icet.ecom.Exceptions.UserAlreadyExistsException;
//import edu.icet.ecom.model.dto.request.LoginRequest;
//import edu.icet.ecom.model.dto.request.UserRegistrationRequest;
//import edu.icet.ecom.model.dto.response.AuthResponse;
//import edu.icet.ecom.model.entity.User;
//import edu.icet.ecom.repository.UserRepository;
//import edu.icet.ecom.security.JwtService;
//import edu.icet.ecom.service.custom.AuthService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.AuthenticationException;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.LocalDateTime;
//
//@Service
//@RequiredArgsConstructor
//public class AuthServiceImpl implements AuthService {
//
//    private final UserRepository userRepository;
//    private final PasswordEncoder passwordEncoder;
//    private final JwtService jwtService;
//    private final AuthenticationManager authenticationManager;
//
//    @Override
//    @Transactional
//    public AuthResponse register(UserRegistrationRequest request) {
//        // Check if passwords match
//        if (!request.getPassword().equals(request.getConfirmPassword())) {
//            throw new InvalidCredentialsException("Passwords do not match");
//        }
//
//        // Check if user already exists
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
//        }
//
//        // Create new user
//        User user = User.builder()
//                .fullname(request.getFullname())
//                .email(request.getEmail())
//                .password(passwordEncoder.encode(request.getPassword()))
//                .createdAt(LocalDateTime.now())
//                .updatedAt(LocalDateTime.now())
//                .build();
//
//        User savedUser = userRepository.save(user);
//
//        // Generate JWT token
//        String jwtToken = jwtService.generateToken(savedUser);
//
//        return AuthResponse.builder()
//                .token(jwtToken)
//                .fullName(savedUser.getFullname())
//                .email(savedUser.getEmail())
//                .role(savedUser.getRole().name())
//                .build();
//    }
//
//    @Override
//    public AuthResponse login(LoginRequest request) {
//        try {
//            // Authenticate user
//            authenticationManager.authenticate(
//                    new UsernamePasswordAuthenticationToken(
//                            request.getEmail(),
//                            request.getPassword()
//                    )
//            );
//
//            // Find user
//            User user = userRepository.findByEmail(request.getEmail())
//                    .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));
//
//            // Generate JWT token
//            String jwtToken = jwtService.generateToken(user);
//
//            return AuthResponse.builder()
//                    .token(jwtToken)
//                    .fullName(user.getFullname())
//                    .email(user.getEmail())
//                    .role(user.getRole().name())
//                    .build();
//
//        } catch (AuthenticationException e) {
//            throw new InvalidCredentialsException("Invalid credentials");
//        }
//    }
//}


package edu.icet.ecom.service.custom.Impl;

import edu.icet.ecom.Exceptions.InvalidCredentialsException;
import edu.icet.ecom.Exceptions.UserAlreadyExistsException;
import edu.icet.ecom.model.dto.request.LoginRequest;
import edu.icet.ecom.model.dto.request.UserRegistrationRequest;
import edu.icet.ecom.model.dto.response.AuthResponse;
import edu.icet.ecom.model.entity.User;
import edu.icet.ecom.repository.UserRepository;
import edu.icet.ecom.security.JwtService;
import edu.icet.ecom.service.custom.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(UserRegistrationRequest request) {
        // Check if passwords match
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new InvalidCredentialsException("Passwords do not match");
        }

        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        // Create new user
        User user = User.builder()
                .fullname(request.getFullname())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(user);

        // Generate JWT token
        String jwtToken = jwtService.generateToken(savedUser);

        log.info("User registered successfully: ID={}, Email={}", savedUser.getId(), savedUser.getEmail());

        // FIXED: Include user ID in response
        return AuthResponse.builder()
                .id(savedUser.getId())           // CRITICAL: Frontend needs this
                .token(jwtToken)
                .fullName(savedUser.getFullname())
                .email(savedUser.getEmail())
                .role(savedUser.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            // Find user
            User user = userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

            // Generate JWT token
            String jwtToken = jwtService.generateToken(user);

            log.info("User logged in successfully: ID={}, Email={}", user.getId(), user.getEmail());

            // FIXED: Include user ID in response
            return AuthResponse.builder()
                    .id(user.getId())              // CRITICAL: Frontend needs this
                    .token(jwtToken)
                    .fullName(user.getFullname())
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

        } catch (AuthenticationException e) {
            log.error("Login failed for email: {}", request.getEmail());
            throw new InvalidCredentialsException("Invalid credentials");
        }
    }
}