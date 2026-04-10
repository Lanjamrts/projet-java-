package com.marketplace.service;

import com.marketplace.model.CartItem;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import com.marketplace.repository.CartItemRepository;
import com.marketplace.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductService productService;

    public CartService(CartItemRepository cartItemRepository,
                       UserRepository userRepository,
                       ProductService productService) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productService = productService;
    }

    // Récupère l'utilisateur depuis Spring Security
    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + username));
    }

    // Retourne tous les articles du panier
    public List<CartItem> getCart(String username) {
        return cartItemRepository.findByUser(getUser(username));
    }

    // Calcule le total du panier
    public Double getTotal(String username) {
        return getCart(username).stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    // Nombre d'articles (pour le badge navbar)
    public int getCount(String username) {
        return cartItemRepository.countByUser(getUser(username));
    }

    // Ajouter un produit au panier (ou incrémenter la quantité)
    @Transactional
    public void addItem(String username, Long productId) {
        User user = getUser(username);
        Product product = productService.findById(productId);

        Optional<CartItem> existing = cartItemRepository.findByUserAndProductId(user, productId);

        if (existing.isPresent()) {
            // Produit déjà dans le panier → on incrémente
            CartItem item = existing.get();
            if (item.getQuantity() < product.getStock()) {
                item.setQuantity(item.getQuantity() + 1);
                cartItemRepository.save(item);
            }
        } else {
            // Nouveau produit dans le panier
            if (product.getStock() > 0) {
                CartItem item = new CartItem();
                item.setUser(user);
                item.setProduct(product);
                item.setQuantity(1);
                cartItemRepository.save(item);
            }
        }
    }

    // Retirer un article du panier
    @Transactional
    public void removeItem(String username, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Article introuvable"));
        // Sécurité : on vérifie que l'article appartient bien à l'utilisateur
        if (item.getUser().getUsername().equals(username)) {
            cartItemRepository.delete(item);
        }
    }

    // Décrémenter la quantité d'un article
    @Transactional
    public void decrementItem(String username, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Article introuvable"));
        if (item.getUser().getUsername().equals(username)) {
            if (item.getQuantity() > 1) {
                item.setQuantity(item.getQuantity() - 1);
                cartItemRepository.save(item);
            } else {
                cartItemRepository.delete(item);
            }
        }
    }

    // Vider complètement le panier
    @Transactional
    public void clearCart(String username) {
        cartItemRepository.deleteByUser(getUser(username));
    }
}