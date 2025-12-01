package ppi.e_commerce.Repository;

import ppi.e_commerce.Model.CartItem;
import ppi.e_commerce.Model.Cart;
import ppi.e_commerce.Model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {
    
    List<CartItem> findByCart(Cart cart);
    
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart = :cart ORDER BY ci.addedAt DESC")
    List<CartItem> findCartItemsByCartOrderByAddedAt(Cart cart);
    
    void deleteByCart(Cart cart);
    
    @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart = :cart")
    Integer getTotalItemsInCart(Cart cart);
    
    @Query("SELECT SUM(ci.unitPrice * ci.quantity) FROM CartItem ci WHERE ci.cart = :cart")
    Double getTotalPriceInCart(Cart cart);
}
