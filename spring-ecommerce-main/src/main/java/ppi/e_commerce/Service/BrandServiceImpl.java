package ppi.e_commerce.Service;

import ppi.e_commerce.Model.Brand;
import ppi.e_commerce.Repository.BrandRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandRepository brandRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Brand> findAll() {
        return brandRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Brand> findActiveBrands() {
        return brandRepository.findActiveBrandsOrderedByName();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Brand> findById(Integer id) {
        return brandRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Brand> findByName(String name) {
        return brandRepository.findByName(name);
    }

    @Override
    public Brand saveBrand(Brand brand) {
        return brandRepository.save(brand);
    }

    @Override
    public Brand updateBrand(Brand brand) {
        return brandRepository.save(brand);
    }

    @Override
    public void deleteBrand(Integer id) {
        brandRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByName(String name) {
        return brandRepository.findByName(name).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countProductsByBrand(Brand brand) {
        return brandRepository.countProductsByBrand(brand);
    }

    @Override
    @Transactional(readOnly = true)
    public long countBrands() {
        return brandRepository.count();
    }
}