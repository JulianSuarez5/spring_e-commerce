package ppi.e_commerce.Model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 2, max = 200, message = "El nombre debe tener entre 2 y 200 caracteres")
    private String name;

    @Column(length = 1000)
    private String description;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a 0")
    private Double price;

    @Min(value = 0, message = "La cantidad no puede ser negativa")
    private int cantidad;

    @Column(length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String model3dUrl; // URL para modelo 3D (GLTF/GLB)

    private boolean active = true;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderDetail> orderDetails;

    public Product() {}

    public Product(String name, String description, Double price, int cantidad, String imageUrl, 
                   Category category, Brand brand) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.cantidad = cantidad;
        this.imageUrl = imageUrl;
        this.category = category;
        this.brand = brand;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Double getPrice() {
        return price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getModel3dUrl() {
        return model3dUrl;
    }

    public int getCantidad() {
        return cantidad;
    }

    public boolean isActive() { 
        return active; 
    }
    
    public boolean getActive() { 
        return active; 
    }

    public LocalDateTime getCreatedAt() { 
        return createdAt; 
    }
    
    public LocalDateTime getUpdatedAt() { 
        return updatedAt; 
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() { 
        return category; 
    }

    public Brand getBrand() { 
        return brand; 
    }

    public List<CartItem> getCartItems() { 
        return cartItems; 
    }

    public List<OrderDetail> getOrderDetails() { 
        return orderDetails; 
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setModel3dUrl(String model3dUrl) {
        this.model3dUrl = model3dUrl;
    }

    public void setActive(boolean active) { 
        this.active = active; 
    }

    public void setCreatedAt(LocalDateTime createdAt) { 
        this.createdAt = createdAt; 
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) { 
        this.updatedAt = updatedAt; 
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCategory(Category category) { 
        this.category = category; 
    }

    public void setBrand(Brand brand) { 
        this.brand = brand; 
    }

    public void setCartItems(List<CartItem> cartItems) { 
        this.cartItems = cartItems; 
    }

    public void setOrderDetails(List<OrderDetail> orderDetails) { 
        this.orderDetails = orderDetails; 
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", cantidad=" + cantidad +
                ", active=" + active +
                '}';
    }
}