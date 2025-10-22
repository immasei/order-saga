package com.example.store.controller;

import com.example.store.dto.account.CreateCustomerDTO;
import com.example.store.dto.account.LoginDTO;
import com.example.store.dto.account.LoginResponseDTO;
import com.example.store.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.Optional;

/**
 * Public Routes
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${deploy.env}")
    private String deployEnv;

    // Signup (cant be used to create an ADMIN account)
    @PostMapping("/signup")
    public ResponseEntity<LoginResponseDTO> signUp(
            @RequestBody @Valid CreateCustomerDTO signUpDto, HttpServletResponse response
    ) {
        LoginResponseDTO loginResponseDto = authService.signUpAndLogin(signUpDto);

        addRefreshCookie(response, loginResponseDto.getRefreshToken());
        return ResponseEntity.ok(loginResponseDto);
    }

    // Login
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(
        @RequestBody @Valid LoginDTO loginDto, HttpServletResponse response
    ) {
        LoginResponseDTO loginResponseDto = authService.login(loginDto);

        addRefreshCookie(response, loginResponseDto.getRefreshToken());
        return ResponseEntity.ok(loginResponseDto);
    }

    // Refresh access token
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDTO> refresh(HttpServletRequest request) {
        // Extract the refresh token from cookies
        String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies())
                        .orElseThrow(() -> new AuthenticationServiceException("No cookies found in request")))
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new AuthenticationServiceException("Refresh token not found in cookies"));

        // generate a new access token
        // we dont rotate refresh token
        LoginResponseDTO loginResponseDto = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(loginResponseDto);
    }

    // Logout
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // clear refresh cookie
        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/refresh");
        cookie.setSecure("production".equals(deployEnv));
        cookie.setMaxAge(0); // expire immediately
        response.addCookie(cookie);

        return ResponseEntity.noContent().build();
    }

    private void addRefreshCookie(HttpServletResponse response, String refreshToken) {
        // store refresh token in an HttpOnly cookie.
        // browser will automatically incl this cookie only when calling /v1/auth/refresh endpoint
        // on the other hand access token is returned to the client
        // and should be included manually in the Authorization header by the client
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/auth/refresh");
        cookie.setSecure("production".equals(deployEnv));
        cookie.setMaxAge(60 * 60 * 24 * 14); // 14 days

        response.addCookie(cookie);
    }

}
