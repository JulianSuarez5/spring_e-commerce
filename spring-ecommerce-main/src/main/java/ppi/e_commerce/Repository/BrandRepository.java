package ppi.e_commerce.Repository;

import ppi.e_commerce.Model.Brand;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Integer> {
    
    Optional<Brand> findByName(String name);
    
    List<Brand> findByActiveTrue();
    
    @Query("SELECT b FROM Brand b WHERE b.active = true ORDER BY b.name")
    List<Brand> findActiveBrandsOrderedByName();
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.brand = :brand")
    Long countProductsByBrand(Brand brand);
}