package com.marketplace.repository;

import com.marketplace.model.CartItem;
import com.marketplace.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Tous les articles du panier d'un utilisateur
    List<CartItem> findByUser(User user);

    // Chercher un article précis dans le panier (pour éviter les doublons)
    Optional<CartItem> findByUserAndProductId(User user, Long productId);

    // Vider le panier d'un utilisateur
    void deleteByUser(User user);

    // Compter le nombre total d'articles dans le panier
    int countByUser(User user);
}