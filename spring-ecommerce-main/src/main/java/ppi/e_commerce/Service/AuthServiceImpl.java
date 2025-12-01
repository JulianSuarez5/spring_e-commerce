package ppi.e_commerce.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ppi.e_commerce.Model.User;
import ppi.e_commerce.Repository.UserRepository;
import ppi.e_commerce.Utils.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthServiceImpl {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Restablecer contraseña - Envía contraseña temporal por correo
     */
    @Transactional
    public boolean restablecerContra(String correo) {
        log.info("🔐 Solicitud de restablecimiento para: {}", correo);
        
        Optional<User> optUser = userRepository.findByEmail(correo);
        
        if (optUser.isEmpty()) {
            log.warn("⚠️ Email no encontrado: {}", correo);
            // Por seguridad, siempre devolver true (no revelar si existe el email)
            return true;
        }

        User usuario = optUser.get();
        
        // Generar contraseña temporal segura
        String nuevaClave = PasswordUtil.generarClaveSegura();
        String hash = PasswordUtil.encriptar(nuevaClave);

        // Guardar contraseña temporal
        usuario.setTempPasswordHash(hash);
        usuario.setUsingTempPassword(true);
        usuario.setTempPasswordExpiry(LocalDateTime.now().plusMinutes(60));
        userRepository.save(usuario);

        log.info("✅ Contraseña temporal generada para: {}", usuario.getUsername());
        log.debug("🔑 Contraseña temporal: {}", nuevaClave); // Solo para debug

        // Enviar correo
        String nombre = usuario.getName() != null ? usuario.getName() : usuario.getUsername();
        boolean enviado = emailService.enviarCorreoRecuperacion(correo, nombre, nuevaClave);
        
        if (enviado) {
            log.info("📧 Correo de recuperación enviado a: {}", correo);
        } else {
            log.error("❌ Error al enviar correo a: {}", correo);
        }
        
        return enviado;
    }

    /**
     * Verificar si la contraseña temporal es válida
     */
    public boolean verificarContrasenaTemporal(User usuario, String contrasena) {
        if (!usuario.isUsingTempPassword() || usuario.getTempPasswordExpiry() == null) {
            log.debug("Usuario no está usando contraseña temporal");
            return false;
        }
        
        if (LocalDateTime.now().isAfter(usuario.getTempPasswordExpiry())) {
            log.warn("⏰ Contraseña temporal expirada para: {}", usuario.getUsername());
            return false;
        }
        
        boolean valida = PasswordUtil.verificar(contrasena, usuario.getTempPasswordHash());
        log.debug("Verificación de contraseña temporal: {}", valida ? "✅" : "❌");
        
        return valida;
    }

    /**
     * Cambiar a contraseña permanente
     */
    @Transactional
    public void cambiarAPasswordPermanente(User usuario, String nuevaContrasena) {
        log.info("🔄 Cambiando a contraseña permanente para: {}", usuario.getUsername());
        
        String hash = PasswordUtil.encriptar(nuevaContrasena);
        usuario.setPassword(hash);
        usuario.setUsingTempPassword(false);
        usuario.setTempPasswordHash(null);
        usuario.setTempPasswordExpiry(null);
        
        userRepository.save(usuario);
        log.info("✅ Contraseña permanente establecida");
    }

    /**
     * Verificar si el usuario está usando contraseña temporal
     */
    public boolean estaUsandoContrasenaTemporal(User usuario) {
        boolean usando = usuario.isUsingTempPassword() && 
                        usuario.getTempPasswordExpiry() != null && 
                        LocalDateTime.now().isBefore(usuario.getTempPasswordExpiry());
        
        if (usando) {
            long minutosRestantes = java.time.Duration.between(
                LocalDateTime.now(), 
                usuario.getTempPasswordExpiry()
            ).toMinutes();
            
            log.info("⏰ Usuario {} tiene contraseña temporal válida por {} minutos más", 
                    usuario.getUsername(), minutosRestantes);
        }
        
        return usando;
    }

    /**
     * Limpiar contraseñas temporales expiradas (se puede ejecutar periódicamente)
     */
    @Transactional
    public void limpiarContrasenasTemporalesExpiradas() {
        log.info("🧹 Limpiando contraseñas temporales expiradas...");
        
        // Aquí podrías implementar una query que limpie todas las contraseñas temporales expiradas
        // Por ahora, dejamos que se limpien al intentar usarlas
    }
}
