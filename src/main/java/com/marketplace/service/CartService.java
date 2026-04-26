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

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + username));
    }

    public List<CartItem> getCart(String username) {
        return cartItemRepository.findByUser(getUser(username));
    }

    public Double getTotal(String username) {
        return getCart(username).stream()
                .mapToDouble(CartItem::getSubtotal)
                .sum();
    }

    public int getCount(String username) {
        return cartItemRepository.countByUser(getUser(username));
    }

    /**
     * Ajoute un produit au panier (ou incrémente si déjà présent).
     * Lève une exception si le stock est dépassé au lieu d'ignorer silencieusement.
     */
    @Transactional
    public void addItem(String username, Long productId) {
        User user = getUser(username);
        Product product = productService.findById(productId);

        if (product.getStock() <= 0) {
            throw new IllegalStateException(
                    "Le produit « " + product.getName() + " » est en rupture de stock.");
        }

        Optional<CartItem> existing = cartItemRepository.findByUserAndProductId(user, productId);

        if (existing.isPresent()) {
            CartItem item = existing.get();
            if (item.getQuantity() >= product.getStock()) {
                throw new IllegalStateException(
                        "Stock maximum atteint pour « " + product.getName() +
                        " » (" + product.getStock() + " disponible(s)).");
            }
            item.setQuantity(item.getQuantity() + 1);
            cartItemRepository.save(item);
        } else {
            CartItem item = new CartItem();
            item.setUser(user);
            item.setProduct(product);
            item.setQuantity(1);
            cartItemRepository.save(item);
        }
    }

    /**
     * Incrémente un article existant du panier par son ID.
     * Remplace la logique cassée de CartController qui rechargeait tout le panier.
     */
    @Transactional
    public void incrementItem(String username, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Article introuvable"));

        if (!item.getUser().getUsername().equals(username)) {
            throw new SecurityException("Accès refusé.");
        }

        Product product = item.getProduct();
        if (item.getQuantity() >= product.getStock()) {
            throw new IllegalStateException(
                    "Stock maximum atteint pour « " + product.getName() +
                    " » (" + product.getStock() + " disponible(s)).");
        }
        item.setQuantity(item.getQuantity() + 1);
        cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(String username, Long itemId) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Article introuvable"));
        if (item.getUser().getUsername().equals(username)) {
            cartItemRepository.delete(item);
        }
    }

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

    @Transactional
    public void clearCart(String username) {
        cartItemRepository.deleteByUser(getUser(username));
    }
}