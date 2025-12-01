package ppi.e_commerce.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:E-Commerce Pro}")
    private String appName;

    /**
     * Enviar correo de texto simple
     */
    public boolean enviarCorreo(String destinatario, String asunto, String mensaje) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(destinatario);
            msg.setSubject(asunto);
            msg.setText(mensaje);
            
            mailSender.send(msg);
            log.info("✅ Correo enviado a: {}", destinatario);
            return true;
            
        } catch (Exception ex) {
            log.error("❌ Error al enviar correo a {}: {}", destinatario, ex.getMessage());
            return false;
        }
    }

    /**
     * Enviar correo HTML usando templates de Thymeleaf
     */
    public boolean enviarCorreoHtml(String destinatario, String asunto, String templateName, Context context) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            
            // Procesar template con Thymeleaf
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);
            
            mailSender.send(mimeMessage);
            log.info("✅ Correo HTML enviado a: {}", destinatario);
            return true;
            
        } catch (MessagingException ex) {
            log.error("❌ Error al enviar correo HTML a {}: {}", destinatario, ex.getMessage());
            return false;
        }
    }

    /**
     * Enviar correo de recuperación de contraseña
     */
    public boolean enviarCorreoRecuperacion(String destinatario, String nombre, String contrasenaTemporal) {
        Context context = new Context();
        context.setVariable("nombre", nombre);
        context.setVariable("nuevaClave", contrasenaTemporal);
        context.setVariable("tiempoExpiracion", 60);
        context.setVariable("appName", appName);
        
        String asunto = "Recuperación de Contraseña - " + appName;
        
        return enviarCorreoHtml(destinatario, asunto, "email/recuperacion-contrasena", context);
    }
}
