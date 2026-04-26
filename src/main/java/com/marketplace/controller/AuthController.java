package com.marketplace.controller;

import com.marketplace.model.User;
import com.marketplace.service.UserService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute User user,
                           BindingResult result,
                           Model model) {
        if (result.hasErrors()) {
            return "auth/register";
        }
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "duplicate", "Ce nom d'utilisateur est déjà pris.");
            return "auth/register";
        }
        user.setRole(User.Role.USER);
        userService.save(user);
        return "redirect:/login?registered";
    }
}