package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ppi.e_commerce.Model.Order;
import ppi.e_commerce.Model.Payment;
import ppi.e_commerce.Model.User;
import ppi.e_commerce.Model.CartItem;
import ppi.e_commerce.Service.CartService;
import ppi.e_commerce.Repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ppi.e_commerce.Service.PaymentService;
import ppi.e_commerce.Service.OrderService;

import java.util.*;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;
    
    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;
    
    @Autowired
    private UserRepository userRepository;

    /**
     * Obtener usuario actual
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
     * Página de checkout - MEJORADA
     */
    @GetMapping("/checkout")
    public String checkoutPage(Model model, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para continuar");
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartService.getCartItems(currentUser);
        
        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Tu carrito está vacío");
            return "redirect:/cart";
        }

        double cartTotal = cartService.getCartTotal(currentUser);
        int itemCount = cartService.getCartItemCount(currentUser);
        
        // Calcular costos
        double shipping = cartTotal >= 500000 ? 0.0 : 50000.0;
        double tax = cartTotal * 0.19;
        double total = cartTotal + shipping + tax;

        // Preparar datos del pedido para la vista
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("subtotal", cartTotal);
        orderData.put("shippingCost", shipping);
        orderData.put("tax", tax);
        orderData.put("total", total);
        orderData.put("discount", 0.0);
        
        // Preparar información de envío
        Map<String, Object> shippingAddress = new HashMap<>();
        shippingAddress.put("fullName", currentUser.getName() != null ? currentUser.getName() : currentUser.getUsername());
        shippingAddress.put("address", currentUser.getAddress() != null ? currentUser.getAddress() : "Sin dirección registrada");
        shippingAddress.put("city", "Medellín");
        shippingAddress.put("state", "Antioquia");
        shippingAddress.put("phone", currentUser.getPhone() != null ? currentUser.getPhone() : "Sin teléfono");
        
        orderData.put("shippingAddress", shippingAddress);

        // Preparar items para la vista
        List<Map<String, Object>> items = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", cartItem.getProduct().getName());
            item.put("quantity", cartItem.getQuantity());
            item.put("price", cartItem.getUnitPrice());
            item.put("imageUrl", cartItem.getProduct().getImageUrl() != null ? 
                    cartItem.getProduct().getImageUrl() : "https://placehold.co/60x60");
            item.put("sku", "SKU-" + cartItem.getProduct().getId());
            items.add(item);
        }

        model.addAttribute("order", orderData);
        model.addAttribute("items", items);
        model.addAttribute("user", currentUser);
        model.addAttribute("cartTotal", cartTotal);
        model.addAttribute("itemCount", itemCount);
        model.addAttribute("cartItems", cartItems);

        return "payment/checkout";
    }

    /**
     * Procesar el pago (crear orden)
     */
    @PostMapping("/process")
    public String processPayment(@RequestParam(name = "method", defaultValue = "cash") String paymentMethod,
                                RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión");
            return "redirect:/login";
        }

        List<CartItem> cartItems = cartService.getCartItems(currentUser);
        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("warning", "Tu carrito está vacío");
            return "redirect:/cart";
        }

        try {
            System.out.println("🔄 Procesando pago - Método: " + paymentMethod);
            
            // Crear la orden usando el servicio existente
            Order order = orderService.createOrder(currentUser, cartItems);
            
            System.out.println("✅ Orden creada: " + order.getNumber() + " - ID: " + order.getId());

            // Crear el pago
            Payment payment = paymentService.createPayment(order, paymentMethod);
            
            System.out.println("✅ Pago registrado - Método: " + payment.getPaymentMethod());

            // Limpiar el carrito después de crear la orden
            cartService.clearCart(currentUser);
            
            System.out.println("✅ Carrito limpiado");

            // Redirigir a página de éxito
            return "redirect:/payment/success?orderId=" + order.getId() + "&method=" + paymentMethod;

        } catch (Exception e) {
            System.err.println("❌ Error al procesar el pago: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Error al procesar el pago: " + e.getMessage());
            return "redirect:/payment/checkout";
        }
    }

    /**
     * Página de éxito (pedido confirmado)
     */
    @GetMapping("/success")
    public String successPage(@RequestParam(required = false) String orderId,
                             @RequestParam(required = false) String method,
                             Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("orderId", orderId != null ? orderId : "N/A");
        model.addAttribute("method", method != null ? method : "cash");
        model.addAttribute("user", currentUser);

        return "payment/success";
    }

    /**
     * Página de cancelación (pago cancelado)
     */
    @GetMapping("/cancel")
    public String cancelPage(Model model) {
        User currentUser = getCurrentUser();
        model.addAttribute("user", currentUser);
        return "payment/cancel";
    }

    /**
     * Crear pago PayPal (AJAX)
     */
    @PostMapping("/create-paypal")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> createPayPalPayment(@RequestParam Double amount) {
        try {
            Map<String, Object> response = Map.of(
                "success", true,
                "paymentId", "PAYPAL_" + System.currentTimeMillis(),
                "amount", amount,
                "currency", "COP"
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Confirmar pago PayPal (AJAX)
     */
    @PostMapping("/confirm-paypal")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> confirmPayPalPayment(@RequestParam String paymentId, 
                                                                   @RequestParam String payerId) {
        try {
            Payment payment = paymentService.processPayPalPayment(paymentId, payerId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "paymentId", payment.getId(),
                "status", payment.getStatus()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Procesar pago contra entrega (AJAX)
     */
    @PostMapping("/cash-on-delivery")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processCashOnDelivery(@RequestParam Integer orderId) {
        try {
            Optional<Order> orderOpt = orderService.getOrderById(orderId);
            if (orderOpt.isPresent()) {
                Payment payment = paymentService.processCashOnDelivery(orderOpt.get());
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "paymentId", payment.getId(),
                    "status", payment.getStatus()
                ));
            }
            return ResponseEntity.badRequest().body(Map.of("error", "Order not found"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}