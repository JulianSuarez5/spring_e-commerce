package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ppi.e_commerce.Model.Category;
import ppi.e_commerce.Service.CategoryService;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String adminCategories(Model model) {
        model.addAttribute("categories", categoryService.findAll());
        return "Admin/categories/index";
    }

    @GetMapping("/new")
    public String newCategory(Model model) {
        model.addAttribute("category", new Category());
        return "Admin/categories/new";
    }

    @PostMapping
    public String createCategory(@ModelAttribute Category category) {
        categoryService.saveCategory(category);
        return "redirect:/admin/categories?success=created";
    }

    @GetMapping("/edit/{id}")
    public String editCategory(@PathVariable Integer id, Model model) {
        Category category = categoryService.findById(id).orElse(null);
        if (category == null) {
            return "redirect:/admin/categories?error=not_found";
        }
        model.addAttribute("category", category);
        return "Admin/categories/edit";
    }

    @PostMapping("/update/{id}")
    public String updateCategory(@PathVariable Integer id, @ModelAttribute Category category) {
        Category existingCategory = categoryService.findById(id).orElse(null);
        if (existingCategory == null) {
            return "redirect:/admin/categories?error=not_found";
        }
        
        existingCategory.setName(category.getName());
        existingCategory.setDescription(category.getDescription());
        existingCategory.setActive(category.getActive());
        
        categoryService.updateCategory(existingCategory);
        return "redirect:/admin/categories?success=updated";
    }

    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories?success=deleted";
    }
}
