package ppi.e_commerce.Repository;

import ppi.e_commerce.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

//Se agrega JpaSpecificationExecutor para permitir filtros dinámicos
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer>, JpaSpecificationExecutor<Product> {
    
    List<Product> findByActiveTrue();
    
    List<Product> findByCategoryId(Integer categoryId);
    
    List<Product> findByBrandId(Integer brandId);
    
    List<Product> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);
    
    @Query("SELECT p FROM Product p WHERE p.active = true ORDER BY p.createdAt DESC")
    List<Product> findTopSellingProducts(int limit);
    
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.cantidad > 0")
    List<Product> findAvailableProducts();
    
    @Query("SELECT p FROM Product p WHERE p.active = true AND p.cantidad <= 5")
    List<Product> findLowStockProducts();
}
