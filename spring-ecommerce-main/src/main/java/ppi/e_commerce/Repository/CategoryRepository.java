package ppi.e_commerce.Repository;

import ppi.e_commerce.Model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    
    Optional<Category> findByName(String name);
    
    List<Category> findByActiveTrue();
    
    @Query("SELECT c FROM Category c WHERE c.active = true ORDER BY c.name")
    List<Category> findActiveCategoriesOrderedByName();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category = :category")
    Long countProductsByCategory(Category category);
}
