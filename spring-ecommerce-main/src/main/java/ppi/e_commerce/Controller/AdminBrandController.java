package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ppi.e_commerce.Model.Brand;
import ppi.e_commerce.Service.BrandService;

@Controller
@RequestMapping("/admin/brands")
public class AdminBrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping
    public String adminBrands(Model model) {
        model.addAttribute("brands", brandService.findAll());
        return "Admin/brands/index";
    }

    @GetMapping("/new")
    public String newBrand(Model model) {
        model.addAttribute("brand", new Brand());
        return "Admin/brands/new";
    }

    @PostMapping
    public String createBrand(@ModelAttribute Brand brand) {
        brandService.saveBrand(brand);
        return "redirect:/admin/brands?success=created";
    }

    @GetMapping("/edit/{id}")
    public String editBrand(@PathVariable Integer id, Model model) {
        Brand brand = brandService.findById(id).orElse(null);
        if (brand == null) {
            return "redirect:/admin/brands?error=not_found";
        }
        model.addAttribute("brand", brand);
        return "Admin/brands/edit";
    }

    @PostMapping("/update/{id}")
    public String updateBrand(@PathVariable Integer id, @ModelAttribute Brand brand) {
        Brand existingBrand = brandService.findById(id).orElse(null);
        if (existingBrand == null) {
            return "redirect:/admin/brands?error=not_found";
        }
        
        existingBrand.setName(brand.getName());
        existingBrand.setDescription(brand.getDescription());
        existingBrand.setLogoUrl(brand.getLogoUrl());
        existingBrand.setWebsite(brand.getWebsite());
        existingBrand.setActive(brand.getActive());
        
        brandService.updateBrand(existingBrand);
        return "redirect:/admin/brands?success=updated";
    }

    @GetMapping("/delete/{id}")
    public String deleteBrand(@PathVariable Integer id) {
        brandService.deleteBrand(id);
        return "redirect:/admin/brands?success=deleted";
    }
}
