package com.example.store.controller;

import com.example.store.model.Product;
import com.example.store.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Web controller for serving HTML pages
 */
@Controller
@RequiredArgsConstructor
public class WebController {

    private final ProductRepository productRepository;

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password. Please try again.");
        }
        if (logout != null) {
            model.addAttribute("logout", "You have been logged out successfully.");
        }
        return "login";
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Product> products = productRepository.findAll();
        System.out.println("DEBUG: Found " + products.size() + " products");
        model.addAttribute("products", products);
        return "dashboard";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }
}