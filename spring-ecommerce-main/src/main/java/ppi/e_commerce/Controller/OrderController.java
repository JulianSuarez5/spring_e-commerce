package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ppi.e_commerce.Model.Order;
import ppi.e_commerce.Model.User;
import ppi.e_commerce.Model.CartItem;
import ppi.e_commerce.Service.OrderService;
import ppi.e_commerce.Service.CartService;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ppi.e_commerce.Repository.UserRepository userRepository;

    @GetMapping("")
    public String listOrders(Model model) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        java.util.Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            model.addAttribute("orders", java.util.Collections.emptyList());
            return "orders/list";
        }
        User currentUser = userOpt.get();
        List<Order> orders = orderService.getOrdersByUser(currentUser);
        model.addAttribute("orders", orders);
        return "orders/list";
    }
    

    @GetMapping("/admin")
    public String adminOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        return "orders/admin";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Integer id, Model model) {
        Order order = orderService.getOrderById(id).orElse(null);
        if (order != null) {
            model.addAttribute("order", order);
            return "orders/detail";
        }
        return "redirect:/orders";
    }

    @PostMapping("/create")
    @ResponseBody
    public String createOrder(@RequestParam(required = false) String shippingAddress,
                              @RequestParam(required = false) String shippingCity,
                              @RequestParam(required = false) String shippingState,
                              @RequestParam(required = false) String shippingZipCode,
                              @RequestParam(required = false) String notes) {
        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
                return "login_required";
            }
            String username = authentication.getName();
            // get user from repository
            java.util.Optional<User> userOpt = userRepository.findByUsername(username);
            if (userOpt.isEmpty()) return "login_required";
            User currentUser = userOpt.get();

            List<CartItem> cartItems = cartService.getCartItems(currentUser);
            if (!cartItems.isEmpty()) {
                Order order = orderService.createOrder(currentUser, cartItems);
                // set shipping
                order.setShippingAddress(shippingAddress);
                order.setShippingCity(shippingCity);
                order.setShippingState(shippingState);
                order.setShippingZipCode(shippingZipCode);
                order.setNotes(notes);
                orderService.updateOrder(order);
                cartService.clearCart(currentUser);
                return "success:" + order.getId();
            }
            return "error:empty_cart";
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    @PostMapping("/{id}/status")
    @ResponseBody
    public String updateOrderStatus(@PathVariable Integer id, @RequestParam String status) {
        try {
            Order order = orderService.updateOrderStatus(id, status);
            if (order != null) {
                return "success";
            }
            return "error:order_not_found";
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }

    @GetMapping("/{id}/track")
    public String trackOrder(@PathVariable Integer id, Model model) {
        Order order = orderService.getOrderById(id).orElse(null);
        if (order != null) {
            model.addAttribute("order", order);
            return "orders/track";
        }
        return "redirect:/orders";
    }

    @PostMapping("/{id}/cancel")
    @ResponseBody
    public String cancelOrder(@PathVariable Integer id) {
        try {
            Order order = orderService.updateOrderStatus(id, "cancelled");
            if (order != null) {
                return "success";
            }
            return "error:order_not_found";
        } catch (Exception e) {
            return "error:" + e.getMessage();
        }
    }
}
