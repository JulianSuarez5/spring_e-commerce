package ppi.e_commerce.Service;

import ppi.e_commerce.Model.CartItem;
import ppi.e_commerce.Model.Order;
import ppi.e_commerce.Model.User;

import java.util.List;
import java.util.Optional;

public interface OrderService {
    
    // Métodos principales
    Order createOrder(User user, List<CartItem> cartItems);
    
    Optional<Order> getOrderById(Integer id);
    
    List<Order> getOrdersByUser(User user);
    
    List<Order> getAllOrders();
    
    Order updateOrderStatus(Integer orderId, String status);
    
    Order updateOrder(Order order);
    
    void deleteOrder(Integer orderId);
    
    // Métodos de consulta
    List<Order> getOrdersByStatus(String status);
    
    Long countOrdersByUser(User user);
    
    Double getTotalSales();
    
    List<Order> getRecentOrders(int limit);
    
    Long countOrders();
    
    List<Order> findAll();
}