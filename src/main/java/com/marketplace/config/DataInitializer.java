package com.marketplace.config;

import com.marketplace.model.Category;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import com.marketplace.service.CategoryService;
import com.marketplace.service.ProductService;
import com.marketplace.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserService userService,
                               ProductService productService,
                               CategoryService categoryService) {
        return args -> {

            // ── Utilisateurs ──────────────────────────────────────────────
            if (!userService.existsByUsername("admin")) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword("admin123");
                admin.setEmail("admin@market.com");
                admin.setRole(User.Role.ADMIN);
                userService.save(admin);
            }

            if (!userService.existsByUsername("user")) {
                User user = new User();
                user.setUsername("user");
                user.setPassword("user123");
                user.setEmail("user@market.com");
                userService.save(user);
            }

            // ── Catégories ────────────────────────────────────────────────
            // CORRECTION : si la catégorie existe déjà, on la récupère
            // au lieu de laisser la variable à null

            Category electronics;
            if (!categoryService.existsByName("Électronique")) {
                Category c = new Category();
                c.setName("Électronique");
                electronics = categoryService.save(c);
            } else {
                electronics = categoryService.findByName("Électronique");
            }

            Category accessories;
            if (!categoryService.existsByName("Accessoires")) {
                Category c = new Category();
                c.setName("Accessoires");
                accessories = categoryService.save(c);
            } else {
                accessories = categoryService.findByName("Accessoires");
            }

            Category home;
            if (!categoryService.existsByName("Maison")) {
                Category c = new Category();
                c.setName("Maison");
                home = categoryService.save(c);
            } else {
                home = categoryService.findByName("Maison");
            }

            // ── Produits demo ─────────────────────────────────────────────
            if (productService.count() == 0) {
                Product p1 = new Product();
                p1.setName("Laptop Pro");
                p1.setDescription("Ordinateur portable haute performance, idéal pour les développeurs.");
                p1.setPrice(999.99);
                p1.setStock(10);
                p1.setCategory(electronics);
                productService.save(p1);

                Product p2 = new Product();
                p2.setName("Souris sans fil");
                p2.setDescription("Souris ergonomique sans fil, autonomie 12 mois.");
                p2.setPrice(29.99);
                p2.setStock(50);
                p2.setCategory(accessories);
                productService.save(p2);

                Product p3 = new Product();
                p3.setName("Clavier mécanique");
                p3.setDescription("Clavier mécanique rétroéclairé RGB, switches Cherry MX Blue.");
                p3.setPrice(89.99);
                p3.setStock(25);
                p3.setCategory(accessories);
                productService.save(p3);

                Product p4 = new Product();
                p4.setName("Écran 27 pouces 4K");
                p4.setDescription("Écran IPS 4K 27 pouces, 144Hz, idéal pour le gaming et la création.");
                p4.setPrice(449.99);
                p4.setStock(8);
                p4.setCategory(electronics);
                productService.save(p4);

                Product p5 = new Product();
                p5.setName("Lampe de bureau LED");
                p5.setDescription("Lampe LED à intensité réglable, protection des yeux.");
                p5.setPrice(34.99);
                p5.setStock(0);
                p5.setCategory(home);
                productService.save(p5);

                Product p6 = new Product();
                p6.setName("Casque audio Bluetooth");
                p6.setDescription("Casque sans fil avec réduction de bruit active, 30h d'autonomie.");
                p6.setPrice(149.99);
                p6.setStock(3);
                p6.setCategory(electronics);
                productService.save(p6);
            }
        };
    }
}