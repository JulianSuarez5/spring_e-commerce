package ppi.e_commerce.Service;

import ppi.e_commerce.Model.Cart;
import ppi.e_commerce.Model.CartItem;
import ppi.e_commerce.Model.Product;
import ppi.e_commerce.Model.User;
import java.util.List;

public interface CartService {
    Cart getOrCreateCart(User user);
    CartItem addToCart(User user, Product product, Integer quantity);
    CartItem updateCartItem(Integer cartItemId, Integer quantity);
    void removeFromCart(Integer cartItemId);
    void clearCart(User user);
    List<CartItem> getCartItems(User user);
    double getCartTotal(User user);
    int getCartItemCount(User user);
    boolean isProductInCart(User user, Product product);
}