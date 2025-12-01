package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ppi.e_commerce.Model.User;
import ppi.e_commerce.Repository.UserRepository;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    /**
     * Perfil de usuario normal (clientes)
     */
    @GetMapping("/profile")
    public String userProfile(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        model.addAttribute("user", user);
        return "profile"; // Debe existir: templates/profile.html
    }

    /**
     * Actualizar perfil de usuario
     */
    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String phone,
                                @RequestParam String address,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        if (authentication == null) {
            return "redirect:/login";
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Verificar si el email ya está en uso por otro usuario
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (!existingUser.getId().equals(user.getId())) {
                throw new RuntimeException("El email ya está en uso");
            }
        });

        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress(address);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("success", "Perfil actualizado exitosamente");
        return "redirect:/profile";
    }

    // /**
    //  * Cambiar contraseña
    //  */
    // @PostMapping("/profile/change-password")
    // public String changePassword(@RequestParam String currentPassword,
    //                               @RequestParam String newPassword,
    //                               @RequestParam String confirmPassword,
    //                               Authentication authentication,
    //                               RedirectAttributes redirectAttributes) {
    //     if (authentication == null) {
    //         return "redirect:/login";
    //     }

    //     if (!newPassword.equals(confirmPassword)) {
    //         redirectAttributes.addFlashAttribute("error", "Las contraseñas nuevas no coinciden");
    //         return "redirect:/profile";
    //     }

    //     String username = authentication.getName();
    //     User user = userRepository.findByUsername(username)
    //             .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

    //     // Verificar contraseña actual
    //     if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
    //         redirectAttributes.addFlashAttribute("error", "La contraseña actual es incorrecta");
    //         return "redirect:/profile";
    //     }

    //     // Actualizar contraseña
    //     user.setPassword(passwordEncoder.encode(newPassword));
    //     userRepository.save(user);

    //     redirectAttributes.addFlashAttribute("success", "Contraseña cambiada exitosamente");
    //     return "redirect:/profile";
    // }
}