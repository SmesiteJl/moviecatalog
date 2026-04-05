package com.example.moviecatalog.controller;

import com.example.moviecatalog.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Неверный логин или пароль, либо аккаунт заблокирован.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model) {
        if (username.isBlank() || email.isBlank() || password.isBlank()) {
            model.addAttribute("errorMessage", "Все поля обязательны.");
            return "register";
        }
        if (userService.existsByUsername(username)) {
            model.addAttribute("errorMessage", "Пользователь с таким именем уже существует.");
            return "register";
        }
        if (userService.existsByEmail(email)) {
            model.addAttribute("errorMessage", "Пользователь с таким email уже существует.");
            return "register";
        }

        userService.register(username, email, password);
        return "redirect:/login?registered";
    }

    @GetMapping("/403")
    public String forbidden() {
        return "error/403";
    }
}
