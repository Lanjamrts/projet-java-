package com.marketplace.controller;

import com.marketplace.model.Order;
import com.marketplace.service.CartService;
import com.marketplace.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * Endpoint JSON appelé par le polling JavaScript côté user.
     * Retourne un map { orderId -> statut } pour toutes les commandes de l'utilisateur.
     * Exemple : { "1": "CONFIRMED", "2": "PENDING" }
     */
    @GetMapping("/statuses")
    @ResponseBody
    public Map<String, String> getStatuses(Authentication auth) {
        List<Order> orders = orderService.findOrdersByUser(auth.getName());
        Map<String, String> result = new LinkedHashMap<>();
        for (Order o : orders) {
            result.put(String.valueOf(o.getId()), o.getStatus().name());
        }
        return result;
    }
}