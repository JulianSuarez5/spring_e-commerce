package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ppi.e_commerce.Model.Order;
import ppi.e_commerce.Service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    /**
     * Lista todos los pedidos (ya lo tienes en AdminController, pero es mejor tenerlo aquí)
     */
    @GetMapping("")
    public String listOrders(Model model, Authentication authentication) {
        // Verificar que el usuario sea administrador
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }

        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "Admin/orders/index";
    }

    /**
     * Muestra el detalle de un pedido específico
     */
    @GetMapping("/show/{id}")
    public String showOrder(@PathVariable Integer id, Model model, Authentication authentication) {
        // Verificar que el usuario sea administrador
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }

        Order order = orderService.getOrderById(id).orElse(null);
        if (order == null) {
            return "redirect:/admin/orders?error=order_not_found";
        }

        model.addAttribute("order", order);
        return "Admin/orders/show";
    }

    /**
     * Actualiza el estado de un pedido
     */
    @PostMapping("/status/{id}")
    public String updateOrderStatus(@PathVariable Integer id, 
                                   @RequestParam String status,
                                   RedirectAttributes redirectAttributes,
                                   Authentication authentication) {
        // Verificar que el usuario sea administrador
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }

        try {
            Order order = orderService.updateOrderStatus(id, status);
            if (order != null) {
                redirectAttributes.addFlashAttribute("success", "Estado del pedido actualizado correctamente");
                return "redirect:/admin/orders/show/" + id;
            } else {
                redirectAttributes.addFlashAttribute("error", "Pedido no encontrado");
                return "redirect:/admin/orders";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al actualizar el estado: " + e.getMessage());
            return "redirect:/admin/orders/show/" + id;
        }
    }

    /**
     * Elimina un pedido (opcional, si lo necesitas)
     */
    @PostMapping("/delete/{id}")
    public String deleteOrder(@PathVariable Integer id, 
                            RedirectAttributes redirectAttributes,
                            Authentication authentication) {
        // Verificar que el usuario sea administrador
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }

        try {
            orderService.deleteOrder(id);
            redirectAttributes.addFlashAttribute("success", "Pedido eliminado correctamente");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al eliminar el pedido: " + e.getMessage());
        }
        
        return "redirect:/admin/orders";
    }
}