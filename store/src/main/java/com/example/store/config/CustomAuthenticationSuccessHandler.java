package com.example.store.config;

import com.example.store.dto.account.LoginResponseDTO;
import com.example.store.model.User;
import com.example.store.service.UserService;
import com.example.store.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Custom authentication success handler for form-based login
 */
@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtService jwtService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
        
        User user = (User) authentication.getPrincipal();
        
        // Generate JWT tokens for the authenticated user
        LoginResponseDTO loginResponse = generateTokensForUser(user);
        
        // Store tokens in session for web access
        request.getSession().setAttribute("accessToken", loginResponse.getAccessToken());
        request.getSession().setAttribute("refreshToken", loginResponse.getRefreshToken());
        request.getSession().setAttribute("user", userService.toResponse(user));
        
        // Set refresh token as HTTP-only cookie
        response.addCookie(createRefreshTokenCookie(loginResponse.getRefreshToken()));
        
        // Redirect to dashboard
        response.sendRedirect("/dashboard");
    }
    
    private LoginResponseDTO generateTokensForUser(User user) {
        var userDto = userService.toResponse(user);
        String accessToken = jwtService.generateAccessToken(userDto);
        String refreshToken = jwtService.generateRefreshToken(userDto);
        return new LoginResponseDTO(userDto, accessToken, refreshToken);
    }
    
    private jakarta.servlet.http.Cookie createRefreshTokenCookie(String refreshToken) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // Set to true in production with HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        return cookie;
    }
}
