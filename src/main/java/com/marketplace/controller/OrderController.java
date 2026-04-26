package com.marketplace.controller;

import com.marketplace.service.CartService;
import com.marketplace.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * Controller dédié à l'historique des commandes utilisateur.
 *
 * AVANT : ce controller contenait aussi GET/POST /cart/checkout,
 *         ce qui mélangait la logique panier et commande.
 *
 * APRÈS : il ne gère QUE /orders/** — afficher la liste et le détail
 *         des commandes passées. Le checkout a été déplacé dans CartController.
 *
 * Règle claire :
 *   /cart/**   → CartController  (panier + passage de commande)
 *   /orders/** → OrderController (consultation des commandes passées)
 */
@Controller
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    public OrderController(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping
    public String listOrders(Model model, Authentication auth) {
        String username = auth.getName();
        model.addAttribute("orders", orderService.findOrdersByUser(username));
        model.addAttribute("cartCount", cartService.getCount(username));
        return "orders/list";
    }

    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Long id,
                              Model model,
                              Authentication auth) {
        String username = auth.getName();
        model.addAttribute("order", orderService.findOrderForUser(username, id));
        model.addAttribute("cartCount", cartService.getCount(username));
        return "orders/detail";
    }
}