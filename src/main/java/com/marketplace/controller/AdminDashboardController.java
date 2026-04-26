package com.marketplace.controller;

import com.marketplace.service.CategoryService;
import com.marketplace.service.OrderService;
import com.marketplace.service.ProductService;
import com.marketplace.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller admin dédié au tableau de bord.
 * Conserve UNIQUEMENT les métriques résumées pour la page d'accueil admin.
 * Les listes complètes sont déléguées aux controllers spécialisés.
 */
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final CategoryService categoryService;

    public AdminDashboardController(UserService userService,
                                    ProductService productService,
                                    OrderService orderService,
                                    CategoryService categoryService) {
        this.userService = userService;
        this.productService = productService;
        this.orderService = orderService;
        this.categoryService = categoryService;
    }

    /**
     * Page d'accueil de l'espace admin : affiche uniquement les compteurs.
     * Les données détaillées sont accessibles via les controllers dédiés :
     *   /admin/users, /admin/products, /admin/categories, /admin/orders
     */
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalUsers",      userService.count());
        model.addAttribute("totalProducts",   productService.count());
        model.addAttribute("totalOrders",     orderService.count());
        model.addAttribute("totalCategories", categoryService.count());
        return "admin/dashboard";
    }
}