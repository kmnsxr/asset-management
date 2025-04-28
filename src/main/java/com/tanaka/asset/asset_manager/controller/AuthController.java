package com.tanaka.asset.asset_manager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/login")
    public String login() {
        return "login"; // login.html を表示
    }

    @GetMapping("/register")
    public String register() {
        return "register"; // register.html を表示（後で作る）
    }
}
