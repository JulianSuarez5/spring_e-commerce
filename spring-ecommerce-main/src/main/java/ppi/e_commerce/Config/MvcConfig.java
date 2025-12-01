package ppi.e_commerce.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import java.nio.file.Paths;

/**
 * Configuración de MVC para servir recursos estáticos (imágenes subidas).
 * Esto es CRUCIAL para que las imágenes de productos se muestren.
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // Lee la ruta del directorio de subida desde application.properties
    // Si no se define, usa "./uploads/" (una carpeta 'uploads' junto al JAR)
    @Value("${app.upload-dir:./uploads/}")
    private String uploadDir;
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Resuelve la ruta absoluta al directorio de subidas
        // (ej. "file:/home/user/my-app/uploads/")
        String resolvedUploadPath = Paths.get(uploadDir).toAbsolutePath().toUri().toString();
        
        // Mapea la URL /uploads/** a la carpeta física de subidas
        // Ahora, una imagen en 'uploads/products/img.jpg' será accesible en
        // http://localhost:8081/uploads/products/img.jpg
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resolvedUploadPath);
    }
}
