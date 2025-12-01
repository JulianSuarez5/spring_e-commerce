package ppi.e_commerce.Repository;

import ppi.e_commerce.Model.Cart;
import ppi.e_commerce.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Integer> {
    
    Optional<Cart> findByUser(User user);
    
    @Query("SELECT c FROM Cart c WHERE c.user = :user")
    Optional<Cart> findCartByUser(User user);
    
    boolean existsByUser(User user);
}
