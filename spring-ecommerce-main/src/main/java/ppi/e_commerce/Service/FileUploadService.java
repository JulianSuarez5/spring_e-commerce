package ppi.e_commerce.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileUploadService {
    // Directorio base (ej. "./uploads/"), definido en application.properties
    @Value("${app.upload-dir:./uploads/}")
    private String uploadBaseDir;

    // Subdirectorio específico para productos
    private final String UPLOAD_SUBDIR = "products/";
    
    /**
     * Guardar imagen y retornar la URL web
     */
    public String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IOException("El archivo está vacío");
        }

        // Validar tipo de archivo
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IOException("El archivo debe ser una imagen (ej. JPG, PNG)");
        }

        // Crear directorio si no existe (ej. ./uploads/products/)
        Path uploadPath = Paths.get(uploadBaseDir, UPLOAD_SUBDIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generar nombre único para el archivo
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String uniqueFilename = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(uniqueFilename);

        // Guardar archivo en el sistema de archivos
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Retornar URL relativa (ej. /uploads/products/archivo.jpg)
        // Esto funcionará gracias a MvcConfig.java
        return "/uploads/" + UPLOAD_SUBDIR + uniqueFilename;
    }

    /**
     * Eliminar imagen
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty() || !imageUrl.startsWith("/uploads/")) {
            return; // No es una imagen local que podamos borrar
        }

        try {
            // Extraer el nombre del archivo de la URL
            String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get(uploadBaseDir, UPLOAD_SUBDIR, filename);
            
            Files.deleteIfExists(filePath);
            
        } catch (IOException e) {
            System.err.println("Error al eliminar imagen: " + imageUrl + " | " + e.getMessage());
        }
    }
}
