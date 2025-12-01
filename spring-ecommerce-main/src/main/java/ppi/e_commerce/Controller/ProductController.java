package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ppi.e_commerce.Model.Brand;
import ppi.e_commerce.Model.Category;
import ppi.e_commerce.Model.Product;
import ppi.e_commerce.Service.ProductService;
import ppi.e_commerce.Service.CategoryService;
import ppi.e_commerce.Service.BrandService;

@Controller
@RequestMapping("/products")
public class ProductController {

    private final Logger log = LoggerFactory.getLogger(ProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private BrandService brandService;

    @GetMapping("")
    public String show(Model model,
                       @RequestParam(name = "categoryId", required = false) Integer categoryId,
                       @RequestParam(name = "brandId", required = false) Integer brandId,
                       @RequestParam(name = "q", required = false) String query,
                       @RequestParam(name = "minPrice", required = false) Double minPrice,
                       @RequestParam(name = "maxPrice", required = false) Double maxPrice) {

        log.info("📋 Iniciando búsqueda de productos con filtros:");
        log.info("  - Categoría ID: {}", categoryId);
        log.info("  - Marca ID: {}", brandId);
        log.info("  - Query: {}", query);
        log.info("  - Precio Min: {}", minPrice);
        log.info("  - Precio Max: {}", maxPrice);

        try {
            // Llamar al servicio de filtrado
            List<Product> products = productService.filterProducts(categoryId, brandId, minPrice, maxPrice, query);
            
            log.info("✅ Productos encontrados: {}", products != null ? products.size() : 0);
            
            // Si no hay filtros aplicados y la lista está vacía, cargar todos los productos activos
            if ((products == null || products.isEmpty()) && 
                categoryId == null && brandId == null && query == null && minPrice == null && maxPrice == null) {
                log.info("🔄 No hay filtros. Cargando todos los productos activos...");
                products = productService.findActiveProducts();
                log.info("✅ Productos activos cargados: {}", products.size());
            }
            
            model.addAttribute("products", products);
            
            // Cargar categorías y marcas para los menús de filtro
            try {
                List<Category> categories = categoryService.findActiveCategories();
                List<Brand> brands = brandService.findActiveBrands();
                log.info("📁 Categorías activas: {}", categories.size());
                log.info("🏷️ Marcas activas: {}", brands.size());
                
                model.addAttribute("categories", categories);
                model.addAttribute("brands", brands);
            } catch (Exception e) {
                log.error("❌ Error al cargar categorías/marcas: {}", e.getMessage());
            }
            
            // Devolver los filtros a la vista
            model.addAttribute("selectedCategoryId", categoryId);
            model.addAttribute("selectedBrandId", brandId);
            model.addAttribute("query", query);
            model.addAttribute("minPrice", minPrice);
            model.addAttribute("maxPrice", maxPrice);
            
            log.info("✅ Vista preparada correctamente");
            return "products";
            
        } catch (Exception e) {
            log.error("❌ Error al cargar productos: ", e);
            model.addAttribute("error", "Error al cargar productos: " + e.getMessage());
            return "products";
        }
    }

    @GetMapping("/{id}")
    public String viewProduct(@PathVariable Integer id, Model model) {
        log.info("👁️ Viendo detalles del producto ID: {}", id);
        
        Optional<Product> productOpt = productService.getProductById(id);
        
        if (productOpt.isEmpty()) {
            log.warn("⚠️ Producto no encontrado con ID: {}", id);
            return "redirect:/products?error=not_found";
        }
        
        Product product = productOpt.get();
        model.addAttribute("product", product);
        
        // Productos relacionados (de la misma categoría)
        if (product.getCategory() != null) {
            List<Product> relatedProducts = productService.findByCategory(product.getCategory().getId())
                .stream()
                .filter(p -> !p.getId().equals(product.getId()))
                .limit(4)
                .toList();
            model.addAttribute("relatedProducts", relatedProducts);
            log.info("🔗 Productos relacionados encontrados: {}", relatedProducts.size());
        }
        
        return "Products/detail";
    }
}
