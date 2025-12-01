package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import ppi.e_commerce.Service.*;
import ppi.e_commerce.Model.User;
import ppi.e_commerce.Repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;
    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @GetMapping
    public String adminDashboard(Model model, Authentication authentication) {
        // Verificar que el usuario tenga rol de administrador
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }

        // KPIs Principales
        Double totalSales = orderService.getTotalSales();
        model.addAttribute("totalSales", (totalSales != null) ? totalSales : 0.0);
        model.addAttribute("totalOrders", orderService.countOrders());

        // Métricas Secundarias
        model.addAttribute("totalProducts", productService.countProducts());
        model.addAttribute("totalCategories", categoryService.countCategories());
        model.addAttribute("totalBrands", brandService.countBrands());

        // Contenido Dinámico
        model.addAttribute("recentOrders", orderService.getRecentOrders(5));

        return "Admin/dashboard";
    }

    @GetMapping("/login")
    public String adminLogin() {
        return "auth/admin_login";
    }

    @GetMapping("/users")
    public String listUsers(Model model, Authentication authentication) {
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }

        model.addAttribute("users", userRepository.findAll());
        return "Admin/users/index"; 
    }

    @GetMapping("/users/create")
    public String createUserForm(Model model, Authentication authentication) {
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }

        model.addAttribute("user", new User());
        return "Admin/users/create";
    }

    @PostMapping("/users/create")
    public String createUser(@ModelAttribute User user, Model model, Authentication authentication) {
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            model.addAttribute("error", "El nombre de usuario ya existe");
            model.addAttribute("user", user);
            return "Admin/users/create";
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null || user.getRole().isBlank()) {
            user.setRole("USER");
        }
        userRepository.save(user);
        return "redirect:/admin/users";
    }


    @GetMapping("/profile")
    public String adminProfile() {
        return "Admin/profile";
    }

    @GetMapping("/users/edit/{id}")
    public String editUser(@PathVariable Integer id, Model model) {
        User user = userRepository.findById(id).orElse(null);
        model.addAttribute("user", user);
        return "Admin/users/edit";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(@PathVariable Integer id, @ModelAttribute User user) {
        User existing = userRepository.findById(id).orElse(null);
        if (existing != null) {
            existing.setUsername(user.getUsername());
            existing.setEmail(user.getEmail());
            existing.setRole(user.getRole());
            existing.setActive(user.isActive());
            userRepository.save(existing);
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
        }
        return "redirect:/admin/users?success=deleted"; 
    }
}