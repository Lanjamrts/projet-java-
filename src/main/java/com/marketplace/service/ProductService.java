package com.marketplace.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.marketplace.model.Category;
import com.marketplace.model.Product;
import com.marketplace.repository.CategoryRepository;
import com.marketplace.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public ProductService(ProductRepository productRepository,
                          CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<Product> findAll() {
        return productRepository.findAll();
    }

    // Recherche avec filtres combinés
    public List<Product> search(String q, Long categoryId, Double minPrice,
                                Double maxPrice, Boolean inStock) {
        if ((q == null || q.isBlank()) && categoryId == null
                && minPrice == null && maxPrice == null && inStock == null) {
            return productRepository.findAll();
        }
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return productRepository.search(query, categoryId, minPrice, maxPrice, inStock);
    }

    public Product findById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable : " + id));
    }

    public Product save(Product product) {
        Product saved = productRepository.save(product);
        saved.setQrCode(generateQrCode(saved.getId()));
        return productRepository.save(saved);
    }

    public void update(Long id, Product updated) {
        Product product = findById(id);
        product.setName(updated.getName());
        product.setDescription(updated.getDescription());
        product.setPrice(updated.getPrice());
        product.setStock(updated.getStock());

        // Mise à jour de la catégorie
        if (updated.getCategory() != null && updated.getCategory().getId() != null) {
            Category cat = categoryRepository.findById(updated.getCategory().getId())
                    .orElse(null);
            product.setCategory(cat);
        } else {
            product.setCategory(null);
        }

        productRepository.save(product);
    }

    public void delete(Long id) {
        productRepository.deleteById(id);
    }

    public long count() {
        return productRepository.count();
    }

    private String generateQrCode(Long productId) {
        try {
            String url = baseUrl + "/products/" + productId;
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(url, BarcodeFormat.QR_CODE, 200, 200);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (Exception e) {
            log.error("Erreur génération QR code pour produit {} : {}", productId, e.getMessage());
            return null;
        }
    }
}