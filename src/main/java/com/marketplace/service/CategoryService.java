package com.marketplace.service;

import com.marketplace.model.Category;
import com.marketplace.repository.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable : " + id));
    }

    public Category findByName(String name) {
        return categoryRepository.findByName(name)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable : " + name));
    }

    public Category save(Category category) {
        category.setSlug(toSlug(category.getName()));
        return categoryRepository.save(category);
    }

    /**
     * AJOUT : méthode update() centralisée.
     * Avant, AdminController reconstruisait le slug directement — logique métier
     * dupliquée et hors du service. Désormais tout passe ici.
     */
    public Category update(Long id, Category updated) {
        Category existing = findById(id);
        existing.setName(updated.getName());
        existing.setSlug(toSlug(updated.getName())); // logique slug dans le service, pas dans le controller
        return categoryRepository.save(existing);
    }

    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }

    /** AJOUT : nécessaire pour AdminDashboardController */
    public long count() {
        return categoryRepository.count();
    }

    private String toSlug(String input) {
        return Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }
}