package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ppi.e_commerce.Service.ReportService;
import ppi.e_commerce.Service.ExportService;
import ppi.e_commerce.Model.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    @Autowired
    private ExportService exportService;

    /**
     * Página principal de reportes con dashboard
     */
    @GetMapping("")
    public String reportsIndex(Model model, Authentication authentication) {
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }

        model.addAttribute("salesSummary", reportService.getSalesSummary());
        model.addAttribute("topProducts", reportService.getTopSellingProducts(5));
        model.addAttribute("topCustomers", reportService.getTopCustomers(5));
        model.addAttribute("salesByMonth", reportService.getSalesByMonth(12));
        model.addAttribute("orderStatusCount", reportService.getOrderStatusCount());

        return "Admin/reports/index";
    }

    /**
     * Reporte de ventas por periodo
     */
    @GetMapping("/sales")
    public String salesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model,
            Authentication authentication) {

        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        model.addAttribute("salesReport", reportService.getSalesReport(start, end));
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "Admin/reports/sales";
    }

    /**
     * Reporte de productos
     */
    @GetMapping("/products")
    public String productsReport(Model model, Authentication authentication) {
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }

        model.addAttribute("productsReport", reportService.getProductsReport());
        model.addAttribute("topProducts", reportService.getTopSellingProducts(10));
        model.addAttribute("lowStockProducts", reportService.getLowStockProducts());

        return "Admin/reports/products";
    }

    /**
     * Reporte de clientes
     */
    @GetMapping("/customers")
    public String customersReport(Model model, Authentication authentication) {
        if (authentication == null || !authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/login?error=access_denied";
        }

        model.addAttribute("customersReport", reportService.getCustomersReport());
        model.addAttribute("topCustomers", reportService.getTopCustomers(10));

        return "Admin/reports/customers";
    }

    /**
     * Exportar ventas a PDF
     */
    @GetMapping("/export/sales/pdf")
    public ResponseEntity<byte[]> exportSalesPdf(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Order> orders = reportService.getSalesReport(start, end).getOrders();
        
        String title = String.format("Reporte de Ventas - %s a %s", 
                startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        
        byte[] pdfBytes = exportService.exportSalesReportToPdf(orders, title);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "reporte-ventas.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    /**
     * Exportar ventas a Excel
     */
    @GetMapping("/export/sales/excel")
    public ResponseEntity<byte[]> exportSalesExcel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        if (startDate == null) startDate = LocalDate.now().withDayOfMonth(1);
        if (endDate == null) endDate = LocalDate.now();

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(23, 59, 59);

        List<Order> orders = reportService.getSalesReport(start, end).getOrders();
        byte[] excelBytes = exportService.exportSalesReportToExcel(orders, "Ventas");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "reporte-ventas.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    /**
     * Exportar productos a Excel
     */
    @GetMapping("/export/products/excel")
    public ResponseEntity<byte[]> exportProductsExcel() {
        List<ppi.e_commerce.Model.Product> products = reportService.getProductsReport().getProducts();
        byte[] excelBytes = exportService.exportProductsToExcel(products);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "reporte-productos.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }

    /**
     * API para gráficas - Ventas por mes
     */
    @GetMapping("/api/sales-chart")
    @ResponseBody
    public Map<String, Object> salesChartData(@RequestParam(defaultValue = "12") int months) {
        return reportService.getSalesChartData(months);
    }

    /**
     * API para gráficas - Productos más vendidos
     */
    @GetMapping("/api/top-products-chart")
    @ResponseBody
    public Map<String, Object> topProductsChartData(@RequestParam(defaultValue = "5") int limit) {
        return reportService.getTopProductsChartData(limit);
    }

    /**
     * API para gráficas - Estado de pedidos
     */
    @GetMapping("/api/order-status-chart")
    @ResponseBody
    public Map<String, Object> orderStatusChartData() {
        return reportService.getOrderStatusChartData();
    }
}