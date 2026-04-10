package com.marketplace.controller;

import com.marketplace.service.CartService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // Afficher le panier
    @GetMapping
    public String viewCart(Model model, Authentication auth) {
        String username = auth.getName();
        model.addAttribute("items", cartService.getCart(username));
        model.addAttribute("total", cartService.getTotal(username));
        model.addAttribute("cartCount", cartService.getCount(username));
        return "cart/index";
    }

    // Ajouter un produit au panier
    @PostMapping("/add/{productId}")
    public String addItem(@PathVariable Long productId,
                          Authentication auth,
                          RedirectAttributes ra) {
        cartService.addItem(auth.getName(), productId);
        ra.addFlashAttribute("success", "Produit ajouté au panier !");
        return "redirect:/products/" + productId;
    }

    // Augmenter la quantité d'un article
    @PostMapping("/increment/{itemId}")
    public String increment(@PathVariable Long itemId, Authentication auth) {
        // On récupère le produit lié pour ré-ajouter
        cartService.addItem(auth.getName(),
                cartService.getCart(auth.getName()).stream()
                        .filter(i -> i.getId().equals(itemId))
                        .findFirst()
                        .map(i -> i.getProduct().getId())
                        .orElseThrow());
        return "redirect:/cart";
    }

    // Décrémenter la quantité (supprime si quantité = 1)
    @PostMapping("/decrement/{itemId}")
    public String decrement(@PathVariable Long itemId, Authentication auth) {
        cartService.decrementItem(auth.getName(), itemId);
        return "redirect:/cart";
    }

    // Supprimer un article du panier
    @PostMapping("/remove/{itemId}")
    public String removeItem(@PathVariable Long itemId,
                             Authentication auth,
                             RedirectAttributes ra) {
        cartService.removeItem(auth.getName(), itemId);
        ra.addFlashAttribute("success", "Article retiré du panier.");
        return "redirect:/cart";
    }

    // Vider le panier
    @PostMapping("/clear")
    public String clearCart(Authentication auth, RedirectAttributes ra) {
        cartService.clearCart(auth.getName());
        ra.addFlashAttribute("success", "Panier vidé.");
        return "redirect:/cart";
    }
}