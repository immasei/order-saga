package com.example.bank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

//return html view
@Controller
public class ViewController {
    @GetMapping("/")
    public String index() {
        return "bank";
    }
}
