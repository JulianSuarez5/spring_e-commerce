package ppi.e_commerce.Service;

import ppi.e_commerce.Model.Product;
import ppi.e_commerce.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Importaciones necesarias para JpaSpecificationExecutor
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
// Fin de importaciones

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> getProductById(Integer id) {
        return productRepository.findById(id);
    }

    @Override
    public Product updateProduct(Product product) {
        return productRepository.save(product);
    }

    @Override
    public void deleteProduct(Integer id) {
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countProducts() {
        return productRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByCategory(Integer categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findByBrand(Integer brandId) {
        return productRepository.findByBrandId(brandId);
    }

    /**
     * CORRECCIÓN:
     * El método searchProducts ahora llama al nuevo método filterProducts
     * para mantener la coherencia.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> searchProducts(String query) {
        // Llama al método de filtrado más completo
        return filterProducts(null, null, null, null, query);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findTopSellingProducts(int limit) {
        return productRepository.findTopSellingProducts(limit);
    }

    /**
     * NUEVO MÉTODO:
     * Implementa la lógica de filtrado dinámico usando JPA Specifications.
     * Construye una consulta SQL basada en los filtros que no son nulos.
     */
    @Override
    @Transactional(readOnly = true)
    public List<Product> filterProducts(Integer categoryId, Integer brandId, Double minPrice, Double maxPrice, String query) {
        
        // Se usa una "Specification" para construir la consulta dinámicamente
        return productRepository.findAll((Specification<Product>) (root, criteriaQuery, criteriaBuilder) -> {
            
            List<Predicate> predicates = new ArrayList<>();
            
            // 1. Siempre buscar solo productos activos
            predicates.add(criteriaBuilder.isTrue(root.get("active")));

            // 2. Filtrar por ID de Categoría si se proporciona
            if (categoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("category").get("id"), categoryId));
            }
            
            // 3. Filtrar por ID de Marca si se proporciona
            if (brandId != null) {
                predicates.add(criteriaBuilder.equal(root.get("brand").get("id"), brandId));
            }
            
            // 4. Filtrar por Precio Mínimo si se proporciona
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            
            // 5. Filtrar por Precio Máximo si se proporciona
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice));
            }
            
            // 6. Filtrar por texto de búsqueda (query) si se proporciona
            if (query != null && !query.isBlank()) {
                String queryLower = "%" + query.toLowerCase() + "%";
                // Buscar en el nombre
                Predicate nameLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), queryLower);
                // Buscar en la descripción
                Predicate descLike = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), queryLower);
                // Unir ambas búsquedas con un 'OR'
                predicates.add(criteriaBuilder.or(nameLike, descLike));
            }

            // Combina todos los predicados (filtros) con un 'AND'
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        });
    }
}