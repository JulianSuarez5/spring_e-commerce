package ppi.e_commerce.Service;

import ppi.e_commerce.Model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> findAll();
    List<Category> findActiveCategories();
    Optional<Category> findById(Integer id);
    Optional<Category> findByName(String name);
    Category saveCategory(Category category);
    Category updateCategory(Category category);
    void deleteCategory(Integer id);
    boolean existsByName(String name);
    Long countProductsByCategory(Category category);
    long countCategories();
}
