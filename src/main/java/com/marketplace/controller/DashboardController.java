package com.marketplace.controller;

import com.marketplace.service.ProductService;
import com.marketplace.service.UserService;
import com.marketplace.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;

    public DashboardController(UserService userService, ProductService productService, OrderService orderService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("totalProducts", productService.count());
        
        // Afficher les dernières commandes de l'utilisateur connecté
        if (auth != null) {
            model.addAttribute("userOrders", orderService.findOrdersByUser(auth.getName()));
        }
        
        return "dashboard/index";
    }
}