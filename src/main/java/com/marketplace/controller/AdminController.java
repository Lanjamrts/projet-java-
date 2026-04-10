package com.marketplace.controller;

import com.marketplace.model.Category;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import com.marketplace.service.CategoryService;
import com.marketplace.service.ProductService;
import com.marketplace.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final ProductService productService;
    private final CategoryService categoryService;

    public AdminController(UserService userService,
                           ProductService productService,
                           CategoryService categoryService) {
        this.userService = userService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    // ── Dashboard ─────────────────────────────────────────────────────────

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userService.count());
        model.addAttribute("totalProducts", productService.count());
        model.addAttribute("users", userService.findAll());
        model.addAttribute("products", productService.findAll());
        return "admin/dashboard";
    }

    // ── Users CRUD ────────────────────────────────────────────────────────

    @GetMapping("/users")
    public String userList(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users/list";
    }

    @GetMapping("/users/new")
    public String newUserForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("isNew", true);
        return "admin/users/form";
    }

    @PostMapping("/users/save")
    public String saveUser(@ModelAttribute User user, RedirectAttributes ra) {
        if (userService.existsByUsername(user.getUsername())) {
            ra.addFlashAttribute("error", "Ce nom d'utilisateur existe déjà.");
            return "redirect:/admin/users/new";
        }
        userService.save(user);
        ra.addFlashAttribute("success", "Utilisateur créé avec succès.");
        return "redirect:/admin/users";
    }

    @GetMapping("/users/edit/{id}")
    public String editUserForm(@PathVariable Long id, Model model) {
        model.addAttribute("user", userService.findById(id));
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("isNew", false);
        return "admin/users/form";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user,
                              RedirectAttributes ra) {
        userService.update(id, user);
        ra.addFlashAttribute("success", "Utilisateur mis à jour.");
        return "redirect:/admin/users";
    }

    // CORRECTION : suppression via POST (plus via GET)
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes ra) {
        userService.delete(id);
        ra.addFlashAttribute("success", "Utilisateur supprimé.");
        return "redirect:/admin/users";
    }

    // ── Products CRUD ─────────────────────────────────────────────────────

    @GetMapping("/products")
    public String productList(Model model) {
        model.addAttribute("products", productService.findAll());
        return "admin/products/list";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isNew", true);
        return "admin/products/form";
    }

    @PostMapping("/products/save")
    public String saveProduct(@ModelAttribute Product product, RedirectAttributes ra) {
        productService.save(product);
        ra.addFlashAttribute("success", "Produit créé avec succès.");
        return "redirect:/admin/products";
    }

    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model) {
        model.addAttribute("product", productService.findById(id));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isNew", false);
        return "admin/products/form";
    }

    @PostMapping("/products/update/{id}")
    public String updateProduct(@PathVariable Long id, @ModelAttribute Product product,
                                 RedirectAttributes ra) {
        productService.update(id, product);
        ra.addFlashAttribute("success", "Produit mis à jour.");
        return "redirect:/admin/products";
    }

    // CORRECTION : suppression via POST (plus via GET)
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        productService.delete(id);
        ra.addFlashAttribute("success", "Produit supprimé.");
        return "redirect:/admin/products";
    }

    // ── Categories CRUD ───────────────────────────────────────────────────

    @GetMapping("/categories")
    public String categoryList(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "admin/categories/list";
    }

    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("isNew", true);
        return "admin/categories/form";
    }

    @PostMapping("/categories/save")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes ra) {
        if (categoryService.existsByName(category.getName())) {
            ra.addFlashAttribute("error", "Cette catégorie existe déjà.");
            return "redirect:/admin/categories/new";
        }
        categoryService.save(category);
        ra.addFlashAttribute("success", "Catégorie créée.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/edit/{id}")
    public String editCategoryForm(@PathVariable Long id, Model model) {
        model.addAttribute("category", categoryService.findById(id));
        model.addAttribute("isNew", false);
        return "admin/categories/form";
    }

    @PostMapping("/categories/update/{id}")
    public String updateCategory(@PathVariable Long id,
                                  @ModelAttribute Category category,
                                  RedirectAttributes ra) {
        Category existing = categoryService.findById(id);
        existing.setName(category.getName());
        existing.setSlug(category.getName().toLowerCase().replaceAll("[^a-z0-9]+", "-"));
        categoryService.save(existing);
        ra.addFlashAttribute("success", "Catégorie mise à jour.");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        categoryService.delete(id);
        ra.addFlashAttribute("success", "Catégorie supprimée.");
        return "redirect:/admin/categories";
    }
}