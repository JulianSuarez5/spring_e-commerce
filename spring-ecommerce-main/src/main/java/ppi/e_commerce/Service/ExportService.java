package ppi.e_commerce.Service;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import ppi.e_commerce.Model.Order;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    /**
     * Exporta reporte de ventas a PDF
     */
    public byte[] exportSalesReportToPdf(List<Order> orders, String title) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // Título
            Paragraph titleParagraph = new Paragraph(title)
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(titleParagraph);

            document.add(new Paragraph("\n"));

            // Tabla
            float[] columnWidths = {2, 3, 2, 2, 2};
            Table table = new Table(columnWidths);
            
            // Headers
            table.addHeaderCell("Número Pedido");
            table.addHeaderCell("Cliente");
            table.addHeaderCell("Fecha");
            table.addHeaderCell("Total");
            table.addHeaderCell("Estado");

            // Datos
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            double totalRevenue = 0;
            
            for (Order order : orders) {
                table.addCell(order.getNumber());
                table.addCell(order.getUser() != null ? 
                        (order.getUser().getName() != null ? order.getUser().getName() : order.getUser().getUsername()) 
                        : "N/A");
                table.addCell(order.getCreationDate().format(formatter));
                table.addCell(String.format("$%.2f", order.getTotalPrice()));
                table.addCell(order.getStatus());
                
                if ("completed".equals(order.getStatus())) {
                    totalRevenue += order.getTotalPrice();
                }
            }

            document.add(table);

            // Total
            document.add(new Paragraph("\n"));
            document.add(new Paragraph(String.format("Total de Pedidos: %d", orders.size())).setBold());
            document.add(new Paragraph(String.format("Ingresos Totales: $%.2f", totalRevenue)).setBold());

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF: " + e.getMessage(), e);
        }
    }

    /**
     * Exporta reporte de ventas a Excel
     */
    public byte[] exportSalesReportToExcel(List<Order> orders, String sheetName) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet(sheetName);

            // Estilo para headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Crear header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Número Pedido", "Cliente", "Fecha", "Total", "Estado", "Dirección", "Ciudad"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Estilo para moneda
            CellStyle currencyStyle = workbook.createCellStyle();
            DataFormat format = workbook.createDataFormat();
            currencyStyle.setDataFormat(format.getFormat("$#,##0.00"));

            // Llenar datos
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            double totalRevenue = 0;

            for (Order order : orders) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(order.getNumber());
                row.createCell(1).setCellValue(order.getUser() != null ? 
                        (order.getUser().getName() != null ? order.getUser().getName() : order.getUser().getUsername()) 
                        : "N/A");
                row.createCell(2).setCellValue(order.getCreationDate().format(formatter));
                
                Cell totalCell = row.createCell(3);
                totalCell.setCellValue(order.getTotalPrice());
                totalCell.setCellStyle(currencyStyle);
                
                row.createCell(4).setCellValue(order.getStatus());
                row.createCell(5).setCellValue(order.getShippingAddress() != null ? order.getShippingAddress() : "");
                row.createCell(6).setCellValue(order.getShippingCity() != null ? order.getShippingCity() : "");
                
                if ("completed".equals(order.getStatus())) {
                    totalRevenue += order.getTotalPrice();
                }
            }

            // Fila de totales
            Row totalRow = sheet.createRow(rowNum + 1);
            totalRow.createCell(0).setCellValue("TOTAL:");
            Cell totalValueCell = totalRow.createCell(3);
            totalValueCell.setCellValue(totalRevenue);
            totalValueCell.setCellStyle(currencyStyle);

            // Auto-ajustar columnas
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Convertir a bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Exporta productos a Excel
     */
    public byte[] exportProductsToExcel(List<ppi.e_commerce.Model.Product> products) {
        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Productos");

            // Estilo para headers
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Header
            Row headerRow = sheet.createRow(0);
            String[] columns = {"ID", "Nombre", "Descripción", "Precio", "Cantidad", "Categoría", "Marca", "Activo"};
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Datos
            int rowNum = 1;
            for (ppi.e_commerce.Model.Product product : products) {
                Row row = sheet.createRow(rowNum++);
                
                row.createCell(0).setCellValue(product.getId());
                row.createCell(1).setCellValue(product.getName());
                row.createCell(2).setCellValue(product.getDescription() != null ? product.getDescription() : "");
                row.createCell(3).setCellValue(product.getPrice());
                row.createCell(4).setCellValue(product.getCantidad());
                row.createCell(5).setCellValue(product.getCategory() != null ? product.getCategory().getName() : "");
                row.createCell(6).setCellValue(product.getBrand() != null ? product.getBrand().getName() : "");
                row.createCell(7).setCellValue(product.isActive() ? "Sí" : "No");
            }

            // Auto-ajustar columnas
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            workbook.write(baos);
            workbook.close();
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar Excel de productos: " + e.getMessage(), e);
        }
    }
}