package ppi.e_commerce.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ppi.e_commerce.Model.OrderDetail;
import ppi.e_commerce.Model.Order;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    
    /**
     * Encuentra todos los detalles de un pedido específico
     */
    List<OrderDetail> findByOrder(Order order);
    
    /**
     * Cuenta los detalles de un pedido
     */
    Long countByOrder(Order order);
}