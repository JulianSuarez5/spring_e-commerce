package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ppi.e_commerce.Model.CartItem;
import ppi.e_commerce.Model.Product;
import ppi.e_commerce.Model.User;
import ppi.e_commerce.Repository.UserRepository;
import ppi.e_commerce.Service.CartService;
import ppi.e_commerce.Service.ProductService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Obtener el usuario actual autenticado
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return null;
        }

        String username = auth.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

    /**
     * Ver el carrito
     */
    @GetMapping("")
    public String viewCart(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login?message=login_required";
        }

        List<CartItem> cartItems = cartService.getCartItems(currentUser);
        double cartTotal = cartService.getCartTotal(currentUser);
        int itemCount = cartService.getCartItemCount(currentUser);
        
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("itemCount", itemCount);
        model.addAttribute("user", currentUser);
        
        return "cart";
    }

    /**
     * Agregar producto al carrito (AJAX)
     */
    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(
            @RequestParam Integer productId, 
            @RequestParam(defaultValue = "1") Integer quantity) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                response.put("status", "error");
                response.put("message", "Debes iniciar sesión para agregar productos al carrito");
                response.put("redirectUrl", "/login");
                return ResponseEntity.status(401).body(response);
            }

            Product product = productService.getProductById(productId).orElse(null);
            if (product == null) {
                response.put("status", "error");
                response.put("message", "Producto no encontrado");
                return ResponseEntity.badRequest().body(response);
            }

            if (!product.getActive()) {
                response.put("status", "error");
                response.put("message", "Este producto no está disponible");
                return ResponseEntity.badRequest().body(response);
            }

            if (product.getCantidad() < quantity) {
                response.put("status", "error");
                response.put("message", "Stock insuficiente. Disponible: " + product.getCantidad());
                return ResponseEntity.badRequest().body(response);
            }

            CartItem cartItem = cartService.addToCart(currentUser, product, quantity);
            int newCartCount = cartService.getCartItemCount(currentUser);

            response.put("status", "success");
            response.put("message", "Producto agregado al carrito");
            response.put("cartItemCount", newCartCount);
            response.put("productName", product.getName());
            response.put("itemQuantity", cartItem.getQuantity());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al agregar el producto: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Actualizar cantidad de un item en el carrito (AJAX)
     */
    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateCartItem(
            @RequestParam Integer cartItemId, 
            @RequestParam Integer quantity) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                response.put("status", "error");
                response.put("message", "Sesión expirada");
                return ResponseEntity.status(401).body(response);
            }

            if (quantity <= 0) {
                response.put("status", "error");
                response.put("message", "La cantidad debe ser mayor a 0");
                return ResponseEntity.badRequest().body(response);
            }

            CartItem updatedItem = cartService.updateCartItem(cartItemId, quantity);
            
            if (updatedItem != null) {
                double newTotal = cartService.getCartTotal(currentUser);
                
                response.put("status", "success");
                response.put("message", "Cantidad actualizada");
                response.put("newTotal", newTotal);
                response.put("itemTotal", updatedItem.getTotalPrice());
                
                return ResponseEntity.ok(response);
            } else {
                response.put("status", "error");
                response.put("message", "Item no encontrado");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al actualizar: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Eliminar item del carrito (AJAX)
     */
    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> removeFromCart(@RequestParam Integer cartItemId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                response.put("status", "error");
                response.put("message", "Sesión expirada");
                return ResponseEntity.status(401).body(response);
            }

            cartService.removeFromCart(cartItemId);
            
            double newTotal = cartService.getCartTotal(currentUser);
            int newCount = cartService.getCartItemCount(currentUser);
            
            response.put("status", "success");
            response.put("message", "Producto eliminado del carrito");
            response.put("newTotal", newTotal);
            response.put("cartItemCount", newCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al eliminar: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Vaciar todo el carrito (AJAX)
     */
    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> clearCart() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                response.put("status", "error");
                response.put("message", "Sesión expirada");
                return ResponseEntity.status(401).body(response);
            }

            cartService.clearCart(currentUser);
            
            response.put("status", "success");
            response.put("message", "Carrito vaciado");
            response.put("cartItemCount", 0);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error al vaciar el carrito: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * Obtener contador del carrito (AJAX)
     */
    @GetMapping("/count")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCartCount() {
        Map<String, Object> response = new HashMap<>();
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            response.put("count", 0);
            return ResponseEntity.ok(response);
        }

        int count = cartService.getCartItemCount(currentUser);
        response.put("count", count);
        
        return ResponseEntity.ok(response);
    }
}
