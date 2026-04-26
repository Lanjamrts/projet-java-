package com.marketplace.controller;

import com.marketplace.model.Order;
import com.marketplace.service.CartService;
import com.marketplace.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Controller dédié au panier ET au checkout.
 *
 * AVANT : le checkout était dans OrderController avec des routes /cart/checkout
 *         ce qui mélangeait les responsabilités entre deux controllers.
 *
 * APRÈS : tout ce qui concerne /cart/** est ici, y compris le checkout.
 *         OrderController ne gère plus que /orders/** (liste et détail commande).
 */
@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final OrderService orderService;

    public CartController(CartService cartService, OrderService orderService) {
        this.cartService = cartService;
        this.orderService = orderService;
    }

    // ── Affichage du panier ───────────────────────────────────────────────

    @GetMapping
    public String viewCart(Model model, Authentication auth) {
        String username = auth.getName();
        model.addAttribute("items", cartService.getCart(username));
        model.addAttribute("total", cartService.getTotal(username));
        model.addAttribute("cartCount", cartService.getCount(username));
        return "cart/index";
    }

    // ── Gestion des articles ──────────────────────────────────────────────

    @PostMapping("/add/{productId}")
    public String addItem(@PathVariable Long productId,
                          Authentication auth,
                          RedirectAttributes ra) {
        try {
            cartService.addItem(auth.getName(), productId);
            ra.addFlashAttribute("success", "Produit ajouté au panier !");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + productId;
    }

    @PostMapping("/increment/{itemId}")
    public String increment(@PathVariable Long itemId,
                            Authentication auth,
                            RedirectAttributes ra) {
        try {
            cartService.incrementItem(auth.getName(), itemId);
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/decrement/{itemId}")
    public String decrement(@PathVariable Long itemId, Authentication auth) {
        cartService.decrementItem(auth.getName(), itemId);
        return "redirect:/cart";
    }

    @PostMapping("/remove/{itemId}")
    public String removeItem(@PathVariable Long itemId,
                             Authentication auth,
                             RedirectAttributes ra) {
        cartService.removeItem(auth.getName(), itemId);
        ra.addFlashAttribute("success", "Article retiré du panier.");
        return "redirect:/cart";
    }

    @PostMapping("/clear")
    public String clearCart(Authentication auth, RedirectAttributes ra) {
        cartService.clearCart(auth.getName());
        ra.addFlashAttribute("success", "Panier vidé.");
        return "redirect:/cart";
    }

    // ── Checkout ──────────────────────────────────────────────────────────
    //
    // CORRECTION : ces deux méthodes étaient dans OrderController.
    // Elles utilisent la route /cart/checkout → elles appartiennent ici.
    // OrderController ne doit gérer QUE /orders/** (voir OrderController.java).

    @GetMapping("/checkout")
    public String checkoutForm(Model model, Authentication auth) {
        String username = auth.getName();
        model.addAttribute("items", cartService.getCart(username));
        model.addAttribute("total", cartService.getTotal(username));
        model.addAttribute("cartCount", cartService.getCount(username));
        model.addAttribute("order", new Order());
        return "cart/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@ModelAttribute Order order,
                             Authentication auth,
                             RedirectAttributes ra) {
        String username = auth.getName();
        try {
            Order saved = orderService.createOrderFromCart(
                    username, order.getShippingAddress());
            ra.addFlashAttribute("success", "Votre commande a bien été enregistrée !");
            return "redirect:/orders/" + saved.getId();
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/cart";
        }
    }
}