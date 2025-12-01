package ppi.e_commerce.Service;

import ppi.e_commerce.Model.Brand;
import java.util.List;
import java.util.Optional;

public interface BrandService {
    List<Brand> findAll();
    List<Brand> findActiveBrands();
    Optional<Brand> findById(Integer id);
    Optional<Brand> findByName(String name);
    Brand saveBrand(Brand brand);
    Brand updateBrand(Brand brand);
    void deleteBrand(Integer id);
    boolean existsByName(String name);
    Long countProductsByBrand(Brand brand);
    long countBrands();
}