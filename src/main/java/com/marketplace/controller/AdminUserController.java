package com.marketplace.controller;

import com.marketplace.model.User;
import com.marketplace.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("isNew", true);
        return "admin/users/form";
    }

    @PostMapping("/save")
    public String save(@Valid @ModelAttribute User user,
                       BindingResult result,
                       Model model,
                       RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("isNew", true);
            return "admin/users/form";
        }
        if (userService.existsByUsername(user.getUsername())) {
            result.rejectValue("username", "duplicate", "Ce nom d'utilisateur existe déjà.");
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("isNew", true);
            return "admin/users/form";
        }
        userService.save(user);
        ra.addFlashAttribute("success", "Utilisateur créé avec succès.");
        return "redirect:/admin/users";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("isNew", false);
        return "admin/users/form";
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute User user,
                         BindingResult result,
                         Model model,
                         RedirectAttributes ra) {
        if (result.hasErrors()) {
            model.addAttribute("roles", User.Role.values());
            model.addAttribute("isNew", false);
            return "admin/users/form";
        }
        userService.update(id, user);
        ra.addFlashAttribute("success", "Utilisateur mis à jour.");
        return "redirect:/admin/users";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        userService.delete(id);
        ra.addFlashAttribute("success", "Utilisateur supprimé.");
        return "redirect:/admin/users";
    }
}