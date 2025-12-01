package ppi.e_commerce.Service;

import ppi.e_commerce.Model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    Product saveProduct(Product product);
    Optional<Product> getProductById(Integer id);
    Product updateProduct(Product product);
    void deleteProduct(Integer id);
    List<Product> findAll();
    List<Product> findActiveProducts();
    Long countProducts();
    List<Product> findByCategory(Integer categoryId);
    List<Product> findByBrand(Integer brandId);
    List<Product> searchProducts(String query);
    List<Product> findTopSellingProducts(int limit);

    /**
     * Busca productos aplicando múltiples filtros de forma dinámica.
     */
    List<Product> filterProducts(Integer categoryId, Integer brandId, Double minPrice, Double maxPrice, String query);
}
