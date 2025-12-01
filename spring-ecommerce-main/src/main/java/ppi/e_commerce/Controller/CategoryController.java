package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ppi.e_commerce.Service.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductService productService;

    @GetMapping
    public String categories(Model model) {
        model.addAttribute("categories", categoryService.findActiveCategories());
        return "categories/index";
    }

    @GetMapping("/{id}")
    public String categoryProducts(@PathVariable Integer id, Model model) {
        // Obtener la categoría
        var category = categoryService.findById(id);
        if (category.isEmpty()) {
            return "redirect:/categories?error=not_found";
        }
        
        // Obtener productos de la categoría
        var products = productService.findByCategory(id);
        
        model.addAttribute("category", category.get());
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.findActiveCategories());
        
        return "categories/products";
    }
}

