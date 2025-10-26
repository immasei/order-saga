package com.example.store.config;

import com.example.store.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import static com.example.store.model.enums.UserRole.*;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    private final CustomAuthenticationFailureHandler authenticationFailureHandler;

    private static final String[] PUBLIC_ROUTES = {
            "/error",
            "/api/auth/**",
            "/api/auditlogs/**",
            "/api/delivery/**",
            "/api/inbox/**",
            "/api/orders/**",
            "/api/outbox/**",
            "/api/products/**",
            "/api/product-purchase-history/**",
            "/api/warehouses/**",
            "/api/warehouse-stocks/**",
            "/api/customers",
            "/",
            "/api/auth/**",
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

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        // Configure both JWT and form-based authentication
        httpSecurity
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(PUBLIC_ROUTES).permitAll()
                    .requestMatchers("/api/admins/**")
                        .hasRole(ADMIN.name())
                    .anyRequest().authenticated())
            .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .successHandler(authenticationSuccessHandler)
                    .failureHandler(authenticationFailureHandler)
                    .permitAll())
            .logout(logout -> logout
                    .logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true")
                    .invalidateHttpSession(true)
                    .deleteCookies("refreshToken")
                    .permitAll())
            .csrf(csrfConfig -> csrfConfig.disable())
            .sessionManagement(sessionConfig -> sessionConfig
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                    .maximumSessions(1)
                    .maxSessionsPreventsLogin(false))
            .formLogin(form -> form.disable())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
