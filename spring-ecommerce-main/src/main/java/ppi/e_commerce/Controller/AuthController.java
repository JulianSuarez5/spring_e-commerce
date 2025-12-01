package ppi.e_commerce.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ppi.e_commerce.Model.User;
import ppi.e_commerce.Repository.UserRepository;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error, Model model) {

        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            return "redirect:/";
        }
        if (error != null) {
            model.addAttribute("error", "Credenciales inválidas");
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String && "anonymousUser".equals(authentication.getPrincipal()))) {
            return "redirect:/";
        }

        model.addAttribute("user", new User());
        return "auth/register";
    }

    @PostMapping("/register")
public String registerUser(@RequestParam String name,
                           @RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String phone,
                           @RequestParam String address,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           Model model) {
    if (!password.equals(confirmPassword)) {
        model.addAttribute("error", "Las contraseñas no coinciden");
        return "auth/register";
    }

    if (userRepository.findByUsername(username).isPresent()) {
        model.addAttribute("error", "El nombre de usuario ya existe");
        return "auth/register";
    }

    if (userRepository.findByEmail(email).isPresent()) {
        model.addAttribute("error", "El email ya está registrado");
        return "auth/register";
    }

    User user = new User();
    user.setName(name);
    user.setUsername(username);
    user.setEmail(email);
    user.setPhone(phone);
    user.setAddress(address);
    user.setPassword(passwordEncoder.encode(password));
    user.setRole("USER");
    user.setActive(true);

    userRepository.save(user);
    return "redirect:/login?registered=true";
}

    @Value("${app.admin.registration.token:}")
    private String adminRegistrationToken;

    @GetMapping("/admin/register")
    public String adminRegisterPage(Model model) {
        boolean adminExists = userRepository.existsByRole("ADMIN");
        model.addAttribute("adminExists", adminExists);
        return "auth/admin_register";
    }

    @PostMapping("/admin/register")
    public String registerAdmin(@RequestParam String name,      
                                @RequestParam String username,
                                @RequestParam String email,
                                @RequestParam String phone,      
                                @RequestParam String address,   
                                @RequestParam String password,
                                @RequestParam String confirmPassword,
                                @RequestParam(required = false) String token,
                                Model model) {
        
        boolean adminExists = userRepository.existsByRole("ADMIN");

        if (adminExists) {
            if (adminRegistrationToken == null || adminRegistrationToken.isBlank()) {
                model.addAttribute("error", "Registro de administradores deshabilitado: configure un token en application.properties");
                model.addAttribute("adminExists", true);
                return "auth/admin_register";
            }
            if (token == null || !token.equals(adminRegistrationToken)) {
                model.addAttribute("error", "Código secreto inválido para crear administradores");
                model.addAttribute("adminExists", true);
                return "auth/admin_register";
            }
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            model.addAttribute("adminExists", adminExists);
            return "auth/admin_register";
        }

        if (userRepository.findByUsername(username).isPresent()) {
            model.addAttribute("error", "El nombre de usuario ya existe");
            model.addAttribute("adminExists", adminExists);
            return "auth/admin_register";
        }

        if (userRepository.findByEmail(email).isPresent()) {
            model.addAttribute("error", "El email ya está registrado");
            model.addAttribute("adminExists", adminExists);
            return "auth/admin_register";
        }

        User user = new User();
        user.setName(name);          
        user.setUsername(username);
        user.setEmail(email);
        user.setPhone(phone);         
        user.setAddress(address);   
        user.setPassword(passwordEncoder.encode(password));
        user.setRole("ADMIN");
        user.setActive(true);

        userRepository.save(user);

        return "redirect:/admin/login?registered=true";
    }
}
