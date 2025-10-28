package com.example.store.filter;

import com.example.store.model.User;
import com.example.store.service.JwtService;
import com.example.store.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserService userService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    @Qualifier("handlerExceptionResolver")
    private HandlerExceptionResolver handlerExceptionResolver;

    private static final String[] PUBLIC_ROUTES = {
            "/error",
            "/api/auth/**",
            "/api/customers",
            "/api/products/**",
            "/api/warehouses/**",
            "/api/stocks/**",

            "/api/auditlogs/**",
            "/api/delivery/**",
            "/api/inbox/**",
            "/api/orders/**",
            "/api/outbox/**",
            "/api/product-purchase-history/**",
            "/",
            "/css/**",
            "/js/**",
            "/images/**",
            "/login",
            "/dashboard",
            "/css/**",
            "/js/**",
            "/",                    // Home page
            "/login",               // Login page
            "/images/**",
            "/webjars/**"
    };

    private boolean isPublic(HttpServletRequest request) {
        String path = request.getServletPath();
        for (String p : PUBLIC_ROUTES) {
            if (pathMatcher.match(p, path)) return true;
        }
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Skip entirely for public endpoints
        if (isPublic(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 2) No bearer? Just continue (let Spring handle 401 for protected endpoints)
            final String requestTokenHeader = request.getHeader("Authorization");
            if (requestTokenHeader == null || !requestTokenHeader.startsWith("Bearer")) {
                filterChain.doFilter(request, response);
                return;
            }

            String token = requestTokenHeader.split("Bearer ")[1];
            UUID userId = jwtService.getUserIdFromToken(token); // throws ExpiredJwtException

            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                User user = userService.getUserById(userId);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                authenticationToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
            // call next filter
            filterChain.doFilter(request, response);

            // do smt with the response
            // once your request go to filter chain it also comes back via the filter chain
            // can log the response here
        } catch (Exception ex) {
            handlerExceptionResolver.resolveException(request, response, null, ex);
        }
    }
}
