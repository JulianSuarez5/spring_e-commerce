package ppi.e_commerce.Repository;

import ppi.e_commerce.Model.Order;
import ppi.e_commerce.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    
    List<Order> findByUserOrderByCreationDateDesc(User user);
    
    List<Order> findAllByOrderByCreationDateDesc();
    
    List<Order> findByStatusOrderByCreationDateDesc(String status);
    
    List<Order> findTop10ByOrderByCreationDateDesc();
    
    Long countByUser(User user);
    
    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o WHERE o.status = 'completed'")
    Double getTotalSales();
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    Long countByStatus(String status);
    
    @Query("SELECT o FROM Order o WHERE o.user = :user AND o.status = :status ORDER BY o.creationDate DESC")
    List<Order> findByUserAndStatus(User user, String status);
}
