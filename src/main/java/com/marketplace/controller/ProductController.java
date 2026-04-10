package com.marketplace.controller;

import com.marketplace.service.CartService;
import com.marketplace.service.CategoryService;
import com.marketplace.service.ProductService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;

    public ProductController(ProductService productService,
                              CategoryService categoryService,
                              CartService cartService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.cartService = cartService;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Boolean inStock,
            Model model,
            Authentication auth) {

        model.addAttribute("products",
                productService.search(q, categoryId, minPrice, maxPrice, inStock));
        model.addAttribute("categories", categoryService.findAll());

        // Paramètres pour conserver les valeurs dans le formulaire de recherche
        model.addAttribute("q", q);
        model.addAttribute("selectedCategory", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("inStock", inStock);

        // Nombre d'articles dans le panier pour le badge navbar
        if (auth != null) {
            model.addAttribute("cartCount", cartService.getCount(auth.getName()));
        }

        return "products/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication auth) {
        model.addAttribute("product", productService.findById(id));
        if (auth != null) {
            model.addAttribute("cartCount", cartService.getCount(auth.getName()));
        }
        return "products/detail";
    }
}