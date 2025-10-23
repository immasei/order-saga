package com.example.store.service;

import com.example.store.dto.account.CreateCustomerDTO;
import com.example.store.dto.account.LoginDTO;
import com.example.store.dto.account.LoginResponseDTO;
import com.example.store.dto.account.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import com.example.store.model.account.User;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    public LoginResponseDTO signUpAndLogin(CreateCustomerDTO signUpDto) {
        UserDTO userDto = userService.createUser(signUpDto);
        return issueTokensFor(userDto);
    }

    public LoginResponseDTO login(LoginDTO loginDto) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        UserDTO userDto = userService.toResponse(user);

        return issueTokensFor(userDto);
    }

    public LoginResponseDTO refreshToken(String refreshToken) {
        UUID userId = jwtService.getUserIdFromToken(refreshToken); // throws ExpiredJwtException
        User user = userService.getUserById(userId);
        UserDTO userDto = userService.toResponse(user);

        String accessToken = jwtService.generateAccessToken(userDto);
        return new LoginResponseDTO(userDto, accessToken, refreshToken);
    }

    // -- issue tokens & build response
    private LoginResponseDTO issueTokensFor(UserDTO userDto) {
        String accessToken = jwtService.generateAccessToken(userDto);
        String refreshToken = jwtService.generateRefreshToken(userDto);
        return new LoginResponseDTO(userDto, accessToken, refreshToken);
    }

}
