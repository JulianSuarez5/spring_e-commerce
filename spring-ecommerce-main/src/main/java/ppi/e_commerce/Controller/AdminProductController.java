package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ppi.e_commerce.Model.*;
import ppi.e_commerce.Service.*;
import ppi.e_commerce.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private static final Logger log = LoggerFactory.getLogger(AdminProductController.class);

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtener usuario actual autenticado
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }
        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    @GetMapping
    public String adminProducts(Model model) {
        log.info("=================================");
        log.info("📦 Cargando panel de productos admin");
        
        try {
            List<Product> products = productService.findAll();
            log.info("✅ Total de productos: {}", products != null ? products.size() : 0);
            
            if (products != null && !products.isEmpty()) {
                for (Product p : products) {
                    log.info("  - ID {}: {} | ${} | Stock: {} | Usuario: {}", 
                            p.getId(), 
                            p.getName(), 
                            p.getPrice(), 
                            p.getCantidad(),
                            p.getUser() != null ? p.getUser().getUsername() : "NULL");
                }
            }
            
            model.addAttribute("products", products);
            
        } catch (Exception e) {
            log.error("❌ ERROR: ", e);
            model.addAttribute("error", "Error al cargar productos");
        }
        
        log.info("=================================");
        return "Admin/products/index";
    }

    @GetMapping("/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("brands", brandService.findAll());
        return "Admin/products/new";
    }

    @PostMapping
    public String createProduct(@ModelAttribute Product product,
                               @RequestParam Integer categoryId,
                               @RequestParam Integer brandId,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        
        log.info("📝 Creando nuevo producto: {}", product.getName());
        
        try {
            // OBTENER USUARIO ACTUAL Y ASIGNARLO AL PRODUCTO
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                product.setUser(currentUser);
                log.info("👤 Usuario asignado: {} (ID: {})", currentUser.getUsername(), currentUser.getId());
            } else {
                log.warn("⚠️ No se pudo obtener el usuario actual");
            }
            
            // Manejar imagen
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = fileUploadService.saveImage(imageFile);
                product.setImageUrl(imageUrl);
                log.info("📸 Imagen guardada: {}", imageUrl);
            }
            
            // Asignar categoría y marca
            Category category = categoryService.findById(categoryId).orElse(null);
            Brand brand = brandService.findById(brandId).orElse(null);
            
            if (category != null) {
                product.setCategory(category);
                log.info("📁 Categoría: {}", category.getName());
            }
            
            if (brand != null) {
                product.setBrand(brand);
                log.info("🏷️ Marca: {}", brand.getName());
            }
            
            // Guardar
            Product savedProduct = productService.saveProduct(product);
            log.info("✅ Producto creado con ID: {} | Usuario ID: {}", 
                    savedProduct.getId(), 
                    savedProduct.getUser() != null ? savedProduct.getUser().getId() : "NULL");
            
            return "redirect:/admin/products?success=created";
            
        } catch (Exception e) {
            log.error("❌ Error al crear producto: ", e);
            return "redirect:/admin/products/new?error=" + e.getMessage();
        }
    }

    @GetMapping("/edit/{id}")
    public String editProduct(@PathVariable Integer id, Model model) {
        Product product = productService.getProductById(id).orElse(null);
        
        if (product == null) {
            return "redirect:/admin/products?error=not_found";
        }
        
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("brands", brandService.findAll());
        
        return "Admin/products/edit";
    }

    @PostMapping("/update/{id}")
    public String updateProduct(@PathVariable Integer id,
                               @ModelAttribute Product product,
                               @RequestParam Integer categoryId,
                               @RequestParam Integer brandId,
                               @RequestParam(value = "imageFile", required = false) MultipartFile imageFile) {
        
        try {
            Product existingProduct = productService.getProductById(id).orElse(null);
            
            if (existingProduct == null) {
                return "redirect:/admin/products?error=not_found";
            }
            
            // Manejar imagen
            if (imageFile != null && !imageFile.isEmpty()) {
                if (existingProduct.getImageUrl() != null && existingProduct.getImageUrl().startsWith("/uploads/")) {
                    fileUploadService.deleteImage(existingProduct.getImageUrl());
                }
                String imageUrl = fileUploadService.saveImage(imageFile);
                existingProduct.setImageUrl(imageUrl);
            }
            
            // Actualizar campos
            existingProduct.setName(product.getName());
            existingProduct.setDescription(product.getDescription());
            existingProduct.setPrice(product.getPrice());
            existingProduct.setCantidad(product.getCantidad());
            existingProduct.setActive(product.getActive());
            
            // Actualizar categoría y marca
            Category category = categoryService.findById(categoryId).orElse(null);
            Brand brand = brandService.findById(brandId).orElse(null);
            
            if (category != null) existingProduct.setCategory(category);
            if (brand != null) existingProduct.setBrand(brand);
            
            // MANTENER EL USUARIO ORIGINAL (no cambiar el que registró el producto)
            log.info("🔄 Actualizando producto. Usuario registrador: {}", 
                    existingProduct.getUser() != null ? existingProduct.getUser().getUsername() : "NULL");
            
            productService.updateProduct(existingProduct);
            
            return "redirect:/admin/products?success=updated";
            
        } catch (Exception e) {
            log.error("❌ Error: ", e);
            return "redirect:/admin/products/edit/" + id + "?error=" + e.getMessage();
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id) {
        try {
            Product product = productService.getProductById(id).orElse(null);
            
            if (product != null && product.getImageUrl() != null && product.getImageUrl().startsWith("/uploads/")) {
                fileUploadService.deleteImage(product.getImageUrl());
            }
            
            productService.deleteProduct(id);
            
            return "redirect:/admin/products?success=deleted";
            
        } catch (Exception e) {
            log.error("❌ Error: ", e);
            return "redirect:/admin/products?error=delete_failed";
        }
    }
}
