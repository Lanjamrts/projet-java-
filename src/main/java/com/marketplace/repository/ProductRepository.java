package com.marketplace.repository;

import com.marketplace.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Décrémente le stock d'un produit de façon atomique.
     * La clause "AND stock >= :qty" garantit qu'on ne passe jamais en négatif.
     * Retourne le nombre de lignes mises à jour (0 = stock insuffisant).
     */
    @Modifying
    @Query("UPDATE Product p SET p.stock = p.stock - :qty " +
           "WHERE p.id = :id AND p.stock >= :qty")
    int decrementStock(@Param("id") Long id, @Param("qty") int qty);

    // Recherche par nom (insensible à la casse)
    List<Product> findByNameContainingIgnoreCase(String name);

    // Recherche par catégorie
    List<Product> findByCategoryId(Long categoryId);

    // Recherche combinée : nom + filtre de prix + filtre stock
    @Query("SELECT p FROM Product p WHERE " +
           "(:q IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "   OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%'))) AND " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
           "(:inStock IS NULL OR (:inStock = true AND p.stock > 0) OR :inStock = false)")
    List<Product> search(
            @Param("q") String q,
            @Param("categoryId") Long categoryId,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("inStock") Boolean inStock
    );
}