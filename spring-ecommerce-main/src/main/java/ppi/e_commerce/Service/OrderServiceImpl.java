package ppi.e_commerce.Service;

import ppi.e_commerce.Model.*;
import ppi.e_commerce.Repository.OrderRepository;
import ppi.e_commerce.Repository.OrderDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Override
    public Order createOrder(User user, List<CartItem> cartItems) {
        Order order = new Order();
        order.setUser(user);
        order.setNumber(generateOrderNumber());
        order.setCreationDate(LocalDateTime.now());
        order.setTotalPrice(calculateTotal(cartItems));
        order.setStatus("pending");

        Order savedOrder = orderRepository.save(order);

        // Create order details
        for (CartItem cartItem : cartItems) {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrder(savedOrder);
            orderDetail.setProduct(cartItem.getProduct());
            orderDetail.setName(cartItem.getProduct().getName());
            orderDetail.setPrice(cartItem.getPrice());
            orderDetail.setQuantity(cartItem.getQuantity());
            orderDetail.setTotalPrice(cartItem.getPrice() * cartItem.getQuantity());

            orderDetailRepository.save(orderDetail);
        }

        return savedOrder;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> getOrderById(Integer id) {
        return orderRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByCreationDateDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getAllOrders() {
        return orderRepository.findAllByOrderByCreationDateDesc();
    }

    @Override
    public Order updateOrderStatus(Integer orderId, String status) {
        Order order = orderRepository.findById(orderId).orElse(null);
        if (order != null) {
            order.setStatus(status);
            
            // ✅ Actualizar fechas según el estado
            switch (status) {
                case "shipped":
                    // Establecer fecha de envío si no existe
                    if (order.getShippedDate() == null) {
                        order.setShippedDate(LocalDateTime.now());
                    }
                    break;
                    
                case "completed":
                case "delivered":
                    // Establecer fecha de recepción si no existe
                    if (order.getReceiveDate() == null) {
                        order.setReceiveDate(LocalDateTime.now());
                    }
                    break;
                    
                default:
                    // No hacer nada para pending y cancelled
                    break;
            }
            
            return orderRepository.save(order);
        }
        return null;
    }

    @Override
    public Order updateOrder(Order order) {
        return orderRepository.save(order);
    }

    @Override
    public void deleteOrder(Integer orderId) {
        orderRepository.deleteById(orderId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatusOrderByCreationDateDesc(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOrdersByUser(User user) {
        return orderRepository.countByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalSales() {
        return orderRepository.getTotalSales();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> getRecentOrders(int limit) {
        // Usar el método que ya tienes en el repository
        return orderRepository.findTop10ByOrderByCreationDateDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Long countOrders() {
        return orderRepository.count();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAll();
    }

    // Métodos privados auxiliares
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private Double calculateTotal(List<CartItem> cartItems) {
        return cartItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }
}