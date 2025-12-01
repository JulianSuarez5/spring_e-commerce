package ppi.e_commerce.Utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
public class PasswordUtil {

    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private static final String CARACTERES = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
    private static final int LONGITUD_CLAVE = 12;
    private static final Random random = new SecureRandom();

    /**
     * Genera una contraseña segura aleatoria
     */
    public static String generarClaveSegura() {
        StringBuilder clave = new StringBuilder(LONGITUD_CLAVE);
        
        // Asegurar al menos un carácter de cada tipo
        clave.append(generarCaracterAleatorio("ABCDEFGHIJKLMNOPQRSTUVWXYZ")); // Mayúscula
        clave.append(generarCaracterAleatorio("abcdefghijklmnopqrstuvwxyz")); // Minúscula
        clave.append(generarCaracterAleatorio("0123456789")); // Número
        clave.append(generarCaracterAleatorio("!@#$%&*")); // Símbolo
        
        // Completar el resto de la longitud
        for (int i = 4; i < LONGITUD_CLAVE; i++) {
            clave.append(CARACTERES.charAt(random.nextInt(CARACTERES.length())));
        }
        
        // Mezclar los caracteres
        return mezclarCadena(clave.toString());
    }

    private static char generarCaracterAleatorio(String caracteres) {
        return caracteres.charAt(random.nextInt(caracteres.length()));
    }

    private static String mezclarCadena(String input) {
        char[] caracteres = input.toCharArray();
        for (int i = caracteres.length - 1; i > 0; i--) {
            int indice = random.nextInt(i + 1);
            char temp = caracteres[i];
            caracteres[i] = caracteres[indice];
            caracteres[indice] = temp;
        }
        return new String(caracteres);
    }

    /**
     * Encripta una contraseña usando BCrypt
     */
    public static String encriptar(String contrasenaPlana) {
        if (contrasenaPlana == null || contrasenaPlana.trim().isEmpty()) {
            throw new IllegalArgumentException("La contraseña no puede estar vacía");
        }
        return passwordEncoder.encode(contrasenaPlana);
    }

    /**
     * Verifica si una contraseña plana coincide con el hash
     */
    public static boolean verificar(String contrasenaPlana, String contrasenaHash) {
        if (contrasenaPlana == null || contrasenaHash == null) {
            return false;
        }
        return passwordEncoder.matches(contrasenaPlana, contrasenaHash);
    }

    /**
     * Verifica la fortaleza de una contraseña
     */
    public static boolean esContrasenaSegura(String contrasena) {
        if (contrasena == null || contrasena.length() < 8) {
            return false;
        }
        
        boolean tieneMayuscula = false;
        boolean tieneMinuscula = false;
        boolean tieneNumero = false;
        boolean tieneSimbolo = false;
        
        for (char c : contrasena.toCharArray()) {
            if (Character.isUpperCase(c)) tieneMayuscula = true;
            if (Character.isLowerCase(c)) tieneMinuscula = true;
            if (Character.isDigit(c)) tieneNumero = true;
            if ("!@#$%&*".indexOf(c) >= 0) tieneSimbolo = true;
        }
        
        return tieneMayuscula && tieneMinuscula && tieneNumero && tieneSimbolo;
    }

    /**
     * Genera un mensaje descriptivo sobre la fortaleza de la contraseña
     */
    public static String obtenerFortalezaContrasena(String contrasena) {
        if (contrasena == null || contrasena.isEmpty()) {
            return "La contraseña no puede estar vacía";
        }
        
        if (contrasena.length() < 8) {
            return "La contraseña debe tener al menos 8 caracteres";
        }
        
        boolean tieneMayuscula = false;
        boolean tieneMinuscula = false;
        boolean tieneNumero = false;
        boolean tieneSimbolo = false;
        
        for (char c : contrasena.toCharArray()) {
            if (Character.isUpperCase(c)) tieneMayuscula = true;
            if (Character.isLowerCase(c)) tieneMinuscula = true;
            if (Character.isDigit(c)) tieneNumero = true;
            if ("!@#$%&*".indexOf(c) >= 0) tieneSimbolo = true;
        }
        
        StringBuilder fortaleza = new StringBuilder();
        if (!tieneMayuscula) fortaleza.append("• Debe contener al menos una mayúscula\n");
        if (!tieneMinuscula) fortaleza.append("• Debe contener al menos una minúscula\n");
        if (!tieneNumero) fortaleza.append("• Debe contener al menos un número\n");
        if (!tieneSimbolo) fortaleza.append("• Debe contener al menos un símbolo (!@#$%&*)\n");
        
        if (fortaleza.length() == 0) {
            return "✓ Contraseña segura";
        } else {
            return "La contraseña debe cumplir:\n" + fortaleza.toString();
        }
    }
}