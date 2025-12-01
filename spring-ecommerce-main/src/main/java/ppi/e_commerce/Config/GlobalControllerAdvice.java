package ppi.e_commerce.Config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import ppi.e_commerce.Model.User;
import ppi.e_commerce.Repository.UserRepository;
import ppi.e_commerce.Service.CartService;
import ppi.e_commerce.Service.CategoryService;
import ppi.e_commerce.Service.BrandService;

import java.util.Optional;

/**
 * GlobalControllerAdvice
 * Proporciona datos globales a todas las vistas
 */
@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private CartService cartService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    /**
     * Agregar contador del carrito a todas las vistas
     */
    @ModelAttribute
    public void addCartAttributes(Model model) {
        try {
            User currentUser = getCurrentUser();
            
            if (currentUser != null) {
                int cartItemCount = cartService.getCartItemCount(currentUser);
                model.addAttribute("cartItemCount", cartItemCount);
                model.addAttribute("currentUser", currentUser);
            } else {
                model.addAttribute("cartItemCount", 0);
            }
        } catch (Exception e) {
            model.addAttribute("cartItemCount", 0);
        }
    }

    /**
     * Agregar categorías y marcas a todas las vistas (para el menú)
     */
    @ModelAttribute
    public void addGlobalData(Model model) {
        try {
            model.addAttribute("categories", categoryService.findActiveCategories());
            model.addAttribute("brands", brandService.findActiveBrands());
        } catch (Exception e) {
            // Silenciar errores si no hay datos
        }
    }

    /**
     * Obtener usuario actual autenticado
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        String username = auth.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        
        return userOpt.orElse(null);
    }
}
