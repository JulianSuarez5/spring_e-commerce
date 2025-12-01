package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ppi.e_commerce.Service.OrderService;
import ppi.e_commerce.Service.ProductService;
import ppi.e_commerce.Service.CategoryService;
import ppi.e_commerce.Service.BrandService;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class DashboardController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private BrandService brandService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get dashboard statistics
        Map<String, Object> stats = getDashboardStats();
        model.addAttribute("stats", stats);
        
        // Get recent orders
        model.addAttribute("recentOrders", orderService.getRecentOrders(10));
        
        // Get top products (this would typically be implemented)
        model.addAttribute("topProducts", getTopProducts());
        
        return "Admin/dashboard";
    }

    // @GetMapping("/reports")
    // public String reports(Model model) {
    //     //Añadir título de página para resaltar el enlace en el sidebar
    //     model.addAttribute("pageTitle", "Reportes");

    //     // Datos del reporte
    //     model.addAttribute("salesData", getSalesReportData());
    //     model.addAttribute("orderStatusData", getOrderStatusData());
    //     model.addAttribute("categoryData", getCategoryData());
        
    //     return "Admin/reports";
    // }


    @GetMapping("/analytics")
    public String analytics(Model model) {
        // Analytics data
        model.addAttribute("userGrowth", getUserGrowthData());
        model.addAttribute("salesTrend", getSalesTrendData());
        model.addAttribute("productPerformance", getProductPerformanceData());
        
        return "Admin/analytics";
    }

    private Map<String, Object> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total sales
        Double totalSales = orderService.getTotalSales();
        stats.put("totalSales", totalSales != null ? totalSales : 0.0);
        
        // Total orders
        Long totalOrders = orderService.countOrdersByUser(null); 
        stats.put("totalOrders", totalOrders != null ? totalOrders : 0);
        
        // Total products
        Long totalProducts = productService.countProducts();
        stats.put("totalProducts", totalProducts != null ? totalProducts : 0);
        
        // Total categories
        Long totalCategories = categoryService.countCategories();
        stats.put("totalCategories", totalCategories != null ? totalCategories : 0);
        
        // Total brands
        Long totalBrands = brandService.countBrands();
        stats.put("totalBrands", totalBrands != null ? totalBrands : 0);
        
        return stats;
    }

    private Map<String, Object> getTopProducts() {
        // This would typically query the database for top-selling products
        Map<String, Object> topProducts = new HashMap<>();
        // Implementation would go here
        return topProducts;
    }

    // private Map<String, Object> getSalesReportData() {
    //     // This would typically query sales data by date range
    //     Map<String, Object> salesData = new HashMap<>();
    //     // Implementation would go here
    //     return salesData;
    // }

    // private Map<String, Object> getOrderStatusData() {
    //     // This would typically query order status distribution
    //     Map<String, Object> orderStatusData = new HashMap<>();
    //     // Implementation would go here
    //     return orderStatusData;
    // }

    // private Map<String, Object> getCategoryData() {
    //     // This would typically query category performance
    //     Map<String, Object> categoryData = new HashMap<>();
    //     // Implementation would go here
    //     return categoryData;
    // }

    private Map<String, Object> getUserGrowthData() {
        // This would typically query user growth over time
        Map<String, Object> userGrowth = new HashMap<>();
        // Implementation would go here
        return userGrowth;
    }

    private Map<String, Object> getSalesTrendData() {
        // This would typically query sales trends
        Map<String, Object> salesTrend = new HashMap<>();
        // Implementation would go here
        return salesTrend;
    }

    private Map<String, Object> getProductPerformanceData() {
        // This would typically query product performance metrics
        Map<String, Object> productPerformance = new HashMap<>();
        // Implementation would go here
        return productPerformance;
    }
}
