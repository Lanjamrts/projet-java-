package com.marketplace.config;

import com.marketplace.model.Product;
import com.marketplace.model.User;
import com.marketplace.service.ProductService;
import com.marketplace.service.UserService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserService userService, ProductService productService) {
        return args -> {
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

            if (productService.count() == 0) {
                Product p1 = new Product();
                p1.setName("Laptop Pro");
                p1.setDescription("High performance laptop");
                p1.setPrice(999.99);
                p1.setStock(10);
                productService.save(p1);

                Product p2 = new Product();
                p2.setName("Wireless Mouse");
                p2.setDescription("Ergonomic wireless mouse");
                p2.setPrice(29.99);
                p2.setStock(50);
                productService.save(p2);
            }
        };
    }
}