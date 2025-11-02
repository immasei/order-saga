package com.example.store.service;

import com.example.store.dto.account.CreateCustomerDTO;
import com.example.store.dto.account.LoginDTO;
import com.example.store.dto.account.LoginResponseDTO;
import com.example.store.dto.account.UserDTO;
import com.example.store.security.JwtTokenProvider;
import com.example.store.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponseDTO signUpAndLogin(CreateCustomerDTO signUpDto) {
        UserDTO userDto = userService.createUser(signUpDto);
        return issueTokensFor(userDto);
    }

    public LoginResponseDTO login(LoginDTO loginDto) {
        System.out.println("=== LOGIN ATTEMPT ===");
        System.out.println("Email: " + loginDto.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
            );


            System.out.println("Authentication successful for: " + authentication.getName());

            User user = (User) authentication.getPrincipal();
            UserDTO userDto = userService.toResponse(user);

            String token = jwtTokenProvider.generateToken(user.getEmail());
            System.out.println("JWT Token generated: " + token.substring(0, 20) + "...");

            // You're missing refreshToken variable - fix this
            String refreshToken = jwtTokenProvider.generateToken(user.getEmail() + "_refresh");

            return LoginResponseDTO.builder()
                    .accessToken(token)
                    .refreshToken(refreshToken)
                    .email(user.getEmail())
                    .role(user.getRole().name())
                    .build();

        } catch (Exception e) {
            System.out.println("Authentication failed: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw to see the actual error
        }
    }

    public LoginResponseDTO refreshToken(String refreshToken) {
        // Extract email from refresh token (you might want to add a method for this in JwtTokenProvider)
        String email = jwtTokenProvider.getEmailFromToken(refreshToken.replace("_refresh", ""));
        User user = userService.getUserByEmail(email); // You'll need to add this method to UserService
        UserDTO userDto = userService.toResponse(user);

        String newAccessToken = jwtTokenProvider.generateToken(user.getEmail());
        return LoginResponseDTO.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken) // Return same refresh token (or generate new one)
                .email(user.getEmail())
                .role(String.valueOf(user.getRole()))
                .build();
    }

    private LoginResponseDTO issueTokensFor(UserDTO userDto) {
        String accessToken = jwtTokenProvider.generateToken(userDto.getEmail());
        String refreshToken = jwtTokenProvider.generateToken(userDto.getEmail() + "_refresh");
        return LoginResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .email(userDto.getEmail())
                .role(userDto.getRole())
                .build();
    }
}