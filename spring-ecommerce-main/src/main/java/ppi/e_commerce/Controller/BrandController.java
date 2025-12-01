package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ppi.e_commerce.Model.Brand;
import ppi.e_commerce.Service.BrandService;

import java.util.List;

@Controller
@RequestMapping("/brands")
public class BrandController {

    @Autowired
    private BrandService brandService;

    @GetMapping("")
    public String listBrands(Model model) {
        List<Brand> brands = brandService.findActiveBrands();
        model.addAttribute("brands", brands);
        return "brands/list";
    }

    @GetMapping("/create")
    public String createBrandForm(Model model) {
        model.addAttribute("brand", new Brand());
        return "brands/create";
    }

    @PostMapping("/save")
    public String saveBrand(@ModelAttribute Brand brand) {
        brandService.saveBrand(brand);
        return "redirect:/brands";
    }

    @GetMapping("/edit/{id}")
    public String editBrandForm(@PathVariable Integer id, Model model) {
        Brand brand = brandService.findById(id).orElse(null);
        if (brand != null) {
            model.addAttribute("brand", brand);
            return "brands/edit";
        }
        return "redirect:/brands";
    }

    @PostMapping("/update")
    public String updateBrand(@ModelAttribute Brand brand) {
        brandService.updateBrand(brand);
        return "redirect:/brands";
    }

    @GetMapping("/delete/{id}")
    public String deleteBrand(@PathVariable Integer id) {

        Brand brand = brandService.findById(id).orElse(null);
        if (brand != null) {
            Long productCount = brandService.countProductsByBrand(brand);
            if (productCount > 0) {

                return "redirect:/brands?error=brand_has_products";
            }
            brandService.deleteBrand(id);
        }
        return "redirect:/brands";
    }
}
