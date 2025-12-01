package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ppi.e_commerce.Model.User;
import ppi.e_commerce.Repository.UserRepository;
import ppi.e_commerce.Service.AuthServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class PasswordController {

    private static final Logger log = LoggerFactory.getLogger(PasswordController.class);

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private UserRepository userRepository;

    /**
     * Mostrar formulario de "Olvidé mi contraseña"
     */
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        log.info("📄 Mostrando formulario de recuperación de contraseña");
        return "auth/forgot-password";
    }

    /**
     * Procesar solicitud de recuperación de contraseña
     */
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        log.info("📧 Procesando solicitud de recuperación para: {}", email);
        
        try {
            boolean sent = authService.restablecerContra(email);
            
            // Siempre mostrar mensaje de éxito (seguridad)
            model.addAttribute("success", 
                "Si el correo electrónico existe en nuestro sistema, " +
                "recibirás una contraseña temporal en los próximos minutos. " +
                "Revisa tu bandeja de entrada y también la carpeta de spam.");
            log.info("✅ Solicitud procesada");
            
        } catch (Exception e) {
            log.error("❌ Error al procesar recuperación: ", e);
            model.addAttribute("error", 
                "Ocurrió un error inesperado. Por favor, intenta nuevamente más tarde.");
        }
        
        return "auth/forgot-password";
    }

    /**
     * Mostrar formulario de cambio de contraseña (usuario autenticado con contraseña temporal)
     */
    @GetMapping("/change-password")
    public String showChangePasswordForm(
            @RequestParam(required = false) String temp,
            Authentication authentication,
            Model model) {
        
        log.info("🔐 Mostrando formulario de cambio de contraseña");
        
        if (authentication == null) {
            log.warn("⚠️ Usuario no autenticado intentó acceder a cambio de contraseña");
            return "redirect:/login";
        }
        
        String username = authentication.getName();
        Optional<User> maybeUser = userRepository.findByUsername(username);
        if (maybeUser.isEmpty()) {
            maybeUser = userRepository.findByEmail(username);
        }
        
        if (maybeUser.isEmpty()) {
            log.error("❌ Usuario no encontrado: {}", username);
            return "redirect:/login?error=true";
        }
        
        User user = maybeUser.get();
        
        // Verificar que realmente está usando contraseña temporal
        if (!authService.estaUsandoContrasenaTemporal(user)) {
            log.warn("⚠️ Usuario {} no está usando contraseña temporal", username);
            // Si ya tiene contraseña permanente, redirigir según su rol
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                return "redirect:/admin";
            } else {
                return "redirect:/products";
            }
        }
        
        model.addAttribute("username", user.getUsername());
        model.addAttribute("tempPassword", true);
        
        return "auth/change-password";
    }

    /**
     * Procesar cambio de contraseña
     */
    @PostMapping("/change-password")
    public String processChangePassword(
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            Model model) {
        
        log.info("🔄 Procesando cambio de contraseña");
        
        if (authentication == null) {
            log.warn("⚠️ Usuario no autenticado");
            return "redirect:/login";
        }
        
        // Validar que las contraseñas coincidan
        if (!newPassword.equals(confirmPassword)) {
            log.warn("❌ Las contraseñas no coinciden");
            model.addAttribute("error", "Las contraseñas no coinciden");
            model.addAttribute("tempPassword", true);
            return "auth/change-password";
        }
        
        // Validar longitud mínima
        if (newPassword.length() < 8) {
            log.warn("❌ Contraseña muy corta");
            model.addAttribute("error", "La contraseña debe tener al menos 8 caracteres");
            model.addAttribute("tempPassword", true);
            return "auth/change-password";
        }
        
        // Buscar usuario
        String username = authentication.getName();
        Optional<User> maybeUser = userRepository.findByUsername(username);
        if (maybeUser.isEmpty()) {
            maybeUser = userRepository.findByEmail(username);
        }
        
        if (maybeUser.isEmpty()) {
            log.error("❌ Usuario no encontrado: {}", username);
            return "redirect:/login?error=true";
        }
        
        User user = maybeUser.get();
        
        // Verificar que está usando contraseña temporal
        if (!authService.estaUsandoContrasenaTemporal(user)) {
            log.warn("⚠️ Usuario no tiene contraseña temporal activa");
            model.addAttribute("error", "No tienes una contraseña temporal activa");
            return "auth/change-password";
        }
        
        try {
            // Cambiar a contraseña permanente
            authService.cambiarAPasswordPermanente(user, newPassword);
            log.info("✅ Contraseña cambiada exitosamente para: {}", username);
            
            // Redirigir según el rol
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                return "redirect:/admin?password_changed=true";
            } else {
                return "redirect:/products?password_changed=true";
            }
            
        } catch (Exception e) {
            log.error("❌ Error al cambiar contraseña: ", e);
            model.addAttribute("error", "Error al cambiar la contraseña. Intenta nuevamente.");
            model.addAttribute("tempPassword", true);
            return "auth/change-password";
        }
    }
}
