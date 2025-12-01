package ppi.e_commerce.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ppi.e_commerce.Model.*;
import ppi.e_commerce.Repository.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Resumen general de ventas
     */
    public SalesSummary getSalesSummary() {
        SalesSummary summary = new SalesSummary();
        
        List<Order> allOrders = orderRepository.findAll();
        List<Order> completedOrders = allOrders.stream()
                .filter(o -> "completed".equals(o.getStatus()))
                .collect(Collectors.toList());
        
        summary.setTotalOrders((long) allOrders.size());
        summary.setCompletedOrders((long) completedOrders.size());
        summary.setTotalRevenue(completedOrders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum());
        summary.setAverageOrderValue(completedOrders.isEmpty() ? 0 : 
                summary.getTotalRevenue() / completedOrders.size());
        
        return summary;
    }

    /**
     * Productos más vendidos
     */
    public List<TopProduct> getTopSellingProducts(int limit) {
        List<OrderDetail> allDetails = orderDetailRepository.findAll();
        
        Map<Integer, TopProduct> productMap = new HashMap<>();
        
        for (OrderDetail detail : allDetails) {
            if (detail.getProduct() != null && 
                "completed".equals(detail.getOrder().getStatus())) {
                
                Integer productId = detail.getProduct().getId();
                TopProduct topProduct = productMap.getOrDefault(productId, 
                        new TopProduct(
                            detail.getProduct().getName(),
                            0,
                            0.0
                        ));
                
                topProduct.setQuantitySold(topProduct.getQuantitySold() + detail.getQuantity());
                topProduct.setTotalRevenue(topProduct.getTotalRevenue() + detail.getTotalPrice());
                
                productMap.put(productId, topProduct);
            }
        }
        
        return productMap.values().stream()
                .sorted((a, b) -> Integer.compare(b.getQuantitySold(), a.getQuantitySold()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Mejores clientes
     */
    public List<TopCustomer> getTopCustomers(int limit) {
        List<Order> completedOrders = orderRepository.findByStatusOrderByCreationDateDesc("completed");
        
        Map<Integer, TopCustomer> customerMap = new HashMap<>();
        
        for (Order order : completedOrders) {
            if (order.getUser() != null) {
                Integer userId = order.getUser().getId();
                TopCustomer customer = customerMap.getOrDefault(userId,
                        new TopCustomer(
                            order.getUser().getName() != null ? order.getUser().getName() : order.getUser().getUsername(),
                            order.getUser().getEmail(),
                            0,
                            0.0
                        ));
                
                customer.setOrderCount(customer.getOrderCount() + 1);
                customer.setTotalSpent(customer.getTotalSpent() + order.getTotalPrice());
                
                customerMap.put(userId, customer);
            }
        }
        
        return customerMap.values().stream()
                .sorted((a, b) -> Double.compare(b.getTotalSpent(), a.getTotalSpent()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Ventas por mes
     */
    public List<MonthlySales> getSalesByMonth(int months) {
        List<MonthlySales> salesList = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        
        for (int i = months - 1; i >= 0; i--) {
            LocalDateTime monthStart = now.minusMonths(i).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            LocalDateTime monthEnd = monthStart.plusMonths(1).minusSeconds(1);
            
            List<Order> monthOrders = orderRepository.findAll().stream()
                    .filter(o -> o.getCreationDate().isAfter(monthStart) && 
                                o.getCreationDate().isBefore(monthEnd) &&
                                "completed".equals(o.getStatus()))
                    .collect(Collectors.toList());
            
            double revenue = monthOrders.stream()
                    .mapToDouble(Order::getTotalPrice)
                    .sum();
            
            salesList.add(new MonthlySales(
                    monthStart.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                    (long) monthOrders.size(),
                    revenue
            ));
        }
        
        return salesList;
    }

    /**
     * Conteo de pedidos por estado
     */
    public Map<String, Long> getOrderStatusCount() {
        List<Order> allOrders = orderRepository.findAll();
        
        Map<String, Long> statusCount = new HashMap<>();
        statusCount.put("pending", allOrders.stream().filter(o -> "pending".equals(o.getStatus())).count());
        statusCount.put("shipped", allOrders.stream().filter(o -> "shipped".equals(o.getStatus())).count());
        statusCount.put("completed", allOrders.stream().filter(o -> "completed".equals(o.getStatus())).count());
        statusCount.put("cancelled", allOrders.stream().filter(o -> "cancelled".equals(o.getStatus())).count());
        
        return statusCount;
    }

    /**
     * Reporte detallado de ventas por periodo
     */
    public SalesReport getSalesReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Order> orders = orderRepository.findAll().stream()
                .filter(o -> o.getCreationDate().isAfter(startDate) && 
                            o.getCreationDate().isBefore(endDate))
                .collect(Collectors.toList());
        
        List<Order> completedOrders = orders.stream()
                .filter(o -> "completed".equals(o.getStatus()))
                .collect(Collectors.toList());
        
        SalesReport report = new SalesReport();
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setTotalOrders((long) orders.size());
        report.setCompletedOrders((long) completedOrders.size());
        report.setTotalRevenue(completedOrders.stream().mapToDouble(Order::getTotalPrice).sum());
        report.setOrders(orders);
        
        return report;
    }

    /**
     * Reporte de productos
     */
    public ProductsReport getProductsReport() {
        List<Product> products = productRepository.findAll();
        
        ProductsReport report = new ProductsReport();
        report.setTotalProducts((long) products.size());
        report.setActiveProducts(products.stream().filter(Product::isActive).count());
        report.setProducts(products);
        
        return report;
    }

    /**
     * Productos con stock bajo
     */
    public List<Product> getLowStockProducts() {
        return productRepository.findAll().stream()
                .filter(p -> p.getCantidad() < 10)
                .sorted((a, b) -> Integer.compare(a.getCantidad(), b.getCantidad()))
                .collect(Collectors.toList());
    }

    /**
     * Reporte de clientes
     */
    public CustomersReport getCustomersReport() {
        List<User> users = userRepository.findAll();
        
        CustomersReport report = new CustomersReport();
        report.setTotalCustomers((long) users.size());
        report.setActiveCustomers(users.stream().filter(User::isActive).count());
        report.setUsers(users);
        
        return report;
    }

    /**
     * Datos para gráfica de ventas
     */
    public Map<String, Object> getSalesChartData(int months) {
        List<MonthlySales> sales = getSalesByMonth(months);
        
        Map<String, Object> data = new HashMap<>();
        data.put("labels", sales.stream().map(MonthlySales::getMonth).collect(Collectors.toList()));
        data.put("values", sales.stream().map(MonthlySales::getRevenue).collect(Collectors.toList()));
        
        return data;
    }

    /**
     * Datos para gráfica de productos más vendidos
     */
    public Map<String, Object> getTopProductsChartData(int limit) {
        List<TopProduct> products = getTopSellingProducts(limit);
        
        Map<String, Object> data = new HashMap<>();
        data.put("labels", products.stream().map(TopProduct::getProductName).collect(Collectors.toList()));
        data.put("values", products.stream().map(TopProduct::getQuantitySold).collect(Collectors.toList()));
        
        return data;
    }

    /**
     * Datos para gráfica de estado de pedidos
     */
    public Map<String, Object> getOrderStatusChartData() {
        Map<String, Long> statusCount = getOrderStatusCount();
        
        Map<String, Object> data = new HashMap<>();
        data.put("labels", Arrays.asList("Pendiente", "Enviado", "Completado", "Cancelado"));
        data.put("values", Arrays.asList(
                statusCount.get("pending"),
                statusCount.get("shipped"),
                statusCount.get("completed"),
                statusCount.get("cancelled")
        ));
        
        return data;
    }

    // ===== CLASES INTERNAS PARA REPORTES =====
    
    public static class SalesSummary {
        private Long totalOrders;
        private Long completedOrders;
        private Double totalRevenue;
        private Double averageOrderValue;
        
        // Getters y Setters
        public Long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
        public Long getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(Long completedOrders) { this.completedOrders = completedOrders; }
        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
        public Double getAverageOrderValue() { return averageOrderValue; }
        public void setAverageOrderValue(Double averageOrderValue) { this.averageOrderValue = averageOrderValue; }
    }
    
    public static class TopProduct {
        private String productName;
        private Integer quantitySold;
        private Double totalRevenue;
        
        public TopProduct(String productName, Integer quantitySold, Double totalRevenue) {
            this.productName = productName;
            this.quantitySold = quantitySold;
            this.totalRevenue = totalRevenue;
        }
        
        // Getters y Setters
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public Integer getQuantitySold() { return quantitySold; }
        public void setQuantitySold(Integer quantitySold) { this.quantitySold = quantitySold; }
        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
    }
    
    public static class TopCustomer {
        private String customerName;
        private String email;
        private Integer orderCount;
        private Double totalSpent;
        
        public TopCustomer(String customerName, String email, Integer orderCount, Double totalSpent) {
            this.customerName = customerName;
            this.email = email;
            this.orderCount = orderCount;
            this.totalSpent = totalSpent;
        }
        
        // Getters y Setters
        public String getCustomerName() { return customerName; }
        public void setCustomerName(String customerName) { this.customerName = customerName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getOrderCount() { return orderCount; }
        public void setOrderCount(Integer orderCount) { this.orderCount = orderCount; }
        public Double getTotalSpent() { return totalSpent; }
        public void setTotalSpent(Double totalSpent) { this.totalSpent = totalSpent; }
    }
    
    public static class MonthlySales {
        private String month;
        private Long orderCount;
        private Double revenue;
        
        public MonthlySales(String month, Long orderCount, Double revenue) {
            this.month = month;
            this.orderCount = orderCount;
            this.revenue = revenue;
        }
        
        // Getters y Setters
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
        public Long getOrderCount() { return orderCount; }
        public void setOrderCount(Long orderCount) { this.orderCount = orderCount; }
        public Double getRevenue() { return revenue; }
        public void setRevenue(Double revenue) { this.revenue = revenue; }
    }
    
    public static class SalesReport {
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Long totalOrders;
        private Long completedOrders;
        private Double totalRevenue;
        private List<Order> orders;
        
        // Getters y Setters
        public LocalDateTime getStartDate() { return startDate; }
        public void setStartDate(LocalDateTime startDate) { this.startDate = startDate; }
        public LocalDateTime getEndDate() { return endDate; }
        public void setEndDate(LocalDateTime endDate) { this.endDate = endDate; }
        public Long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(Long totalOrders) { this.totalOrders = totalOrders; }
        public Long getCompletedOrders() { return completedOrders; }
        public void setCompletedOrders(Long completedOrders) { this.completedOrders = completedOrders; }
        public Double getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
        public List<Order> getOrders() { return orders; }
        public void setOrders(List<Order> orders) { this.orders = orders; }
    }
    
    public static class ProductsReport {
        private Long totalProducts;
        private Long activeProducts;
        private List<Product> products;
        
        // Getters y Setters
        public Long getTotalProducts() { return totalProducts; }
        public void setTotalProducts(Long totalProducts) { this.totalProducts = totalProducts; }
        public Long getActiveProducts() { return activeProducts; }
        public void setActiveProducts(Long activeProducts) { this.activeProducts = activeProducts; }
        public List<Product> getProducts() { return products; }
        public void setProducts(List<Product> products) { this.products = products; }
    }
    
    public static class CustomersReport {
        private Long totalCustomers;
        private Long activeCustomers;
        private List<User> users;
        
        // Getters y Setters
        public Long getTotalCustomers() { return totalCustomers; }
        public void setTotalCustomers(Long totalCustomers) { this.totalCustomers = totalCustomers; }
        public Long getActiveCustomers() { return activeCustomers; }
        public void setActiveCustomers(Long activeCustomers) { this.activeCustomers = activeCustomers; }
        public List<User> getUsers() { return users; }
        public void setUsers(List<User> users) { this.users = users; }
    }
}