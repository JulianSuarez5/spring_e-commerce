package ppi.e_commerce.Controller.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ppi.e_commerce.Dto.ProductDto;
import ppi.e_commerce.Model.Product;
import ppi.e_commerce.Service.ProductService;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/products")
@CrossOrigin(origins = "*", maxAge = 3600)
public class ProductApiController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDto>> getAllProducts(
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) Integer brandId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice) {
        
        List<Product> products = productService.filterProducts(categoryId, brandId, minPrice, maxPrice, q);
        
        if (products == null || products.isEmpty()) {
            products = productService.findActiveProducts();
        }
        
        List<ProductDto> productDtos = products.stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProductById(@PathVariable Integer id) {
        Optional<Product> productOpt = productService.getProductById(id);
        
        if (productOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ProductDto dto = convertToDto(productOpt.get());
        return ResponseEntity.ok(dto);
    }

    private ProductDto convertToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setCantidad(product.getCantidad());
        dto.setImageUrl(product.getImageUrl());
        dto.setModel3dUrl(product.getModel3dUrl());
        dto.setActive(product.isActive());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        
        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }
        
        if (product.getBrand() != null) {
            dto.setBrandId(product.getBrand().getId());
            dto.setBrandName(product.getBrand().getName());
        }
        
        // TODO: Agregar model3dUrl cuando se implemente el soporte 3D
        
        return dto;
    }
}

