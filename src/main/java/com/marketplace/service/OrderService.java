package com.marketplace.service;

import com.marketplace.model.CartItem;
import com.marketplace.model.Order;
import com.marketplace.model.OrderItem;
import com.marketplace.model.Product;
import com.marketplace.model.User;
import com.marketplace.repository.OrderRepository;
import com.marketplace.repository.ProductRepository;
import com.marketplace.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository,
                        UserRepository userRepository,
                        ProductRepository productRepository,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable : " + username));
    }

    @Transactional
    public Order createOrderFromCart(String username, String shippingAddress) {
        User user = getUser(username);
        List<CartItem> cartItems = cartService.getCart(username);

        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Votre panier est vide.");
        }

        // ── Étape 1 : vérification du stock pour tous les articles AVANT toute écriture.
        // Si un seul article manque de stock, on lève une exception et rien n'est modifié.
        List<String> ruptures = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            if (product.getStock() < cartItem.getQuantity()) {
                ruptures.add(String.format("« %s » : %d demandé(s), %d disponible(s)",
                        product.getName(), cartItem.getQuantity(), product.getStock()));
            }
        }
        if (!ruptures.isEmpty()) {
            throw new IllegalStateException(
                    "Stock insuffisant pour : " + String.join(", ", ruptures));
        }

        // ── Étape 2 : décrémentation atomique du stock pour chaque article.
        // La requête UPDATE ... WHERE stock >= qty garantit qu'une commande concurrente
        // passée entre la vérification et ici ne peut pas mettre le stock en négatif.
        for (CartItem cartItem : cartItems) {
            int updated = productRepository.decrementStock(
                    cartItem.getProduct().getId(), cartItem.getQuantity());

            // Si updated == 0, un concurrent nous a pris le stock entre temps.
            if (updated == 0) {
                throw new IllegalStateException(
                        "Le produit « " + cartItem.getProduct().getName() +
                        " » vient de passer en rupture. Veuillez mettre à jour votre panier.");
            }
        }

        // ── Étape 3 : création de la commande (inchangé, mais seulement si le stock est ok).
        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress);
        order.setStatus(Order.Status.PENDING);

        List<OrderItem> items = cartItems.stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            // On capture le prix au moment de la commande, pas le prix actuel du produit.
            orderItem.setPrice(cartItem.getProduct().getPrice());
            return orderItem;
        }).collect(Collectors.toList());

        order.getItems().addAll(items);
        order.setTotal(order.calculateTotal());

        Order saved = orderRepository.save(order);
        cartService.clearCart(username);
        return saved;
    }

    public List<Order> findOrdersByUser(String username) {
        return orderRepository.findByUserOrderByCreatedAtDesc(getUser(username));
    }

    public Order findOrderForUser(String username, Long orderId) {
        return orderRepository.findById(orderId)
                .filter(order -> order.getUser().getUsername().equals(username))
                .orElseThrow(() -> new EntityNotFoundException("Commande introuvable."));
    }

    public List<Order> findAllOrders() {
        return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Order findById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Commande introuvable."));
    }

    public void updateStatus(Long orderId, Order.Status status) {
        Order order = findById(orderId);
        order.setStatus(status);
        orderRepository.save(order);
    }

    public long count() {
        return orderRepository.count();
    }
}