package ppi.e_commerce.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.User;
import ppi.e_commerce.Repository.UserRepository;
import ppi.e_commerce.Service.AuthServiceImpl;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.beans.factory.annotation.Autowired;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthServiceImpl authService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/cart/**")
            )
            .authorizeHttpRequests(authz -> authz
                // Rutas públicas (sin autenticación)
                .requestMatchers("/", "/css/**", "/js/**", "/images/**", "/vendor/**", "/webjars/**").permitAll()
                .requestMatchers("/login", "/register", "/admin/login", "/admin/register").permitAll()
                
                // Rutas de recuperación de contraseña (SIN autenticación)
                .requestMatchers("/auth/forgot-password").permitAll()
                
                // APIs públicas
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/products/**").permitAll()
                
                .requestMatchers("/products", "/products/**", "/categories/**", "/brands/**").permitAll()
                .requestMatchers("/error").permitAll()
                
                // APIs protegidas
                .requestMatchers("/api/**").authenticated()
                
                // Rutas de ADMINISTRADOR (SOLO ROLE_ADMIN)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // Rutas de USUARIO AUTENTICADO (USER o ADMIN)
                .requestMatchers("/cart/**", "/orders/**", "/payment/**", "/profile/**").authenticated()
                
                // Cambio de contraseña REQUIERE estar autenticado (con contraseña temporal)
                .requestMatchers("/auth/change-password").authenticated()
                
                // Todas las demás rutas requieren autenticación
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(authenticationSuccessHandler())
                .failureHandler(authenticationFailureHandler())
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)
            )
            .exceptionHandling(exceptions -> exceptions
                .accessDeniedPage("/access-denied")
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*")); // En producción, especificar dominios
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(passwordEncoder());
        authProvider.setUserDetailsService(userDetailsService(userRepository, authService));
        return authProvider;
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository, AuthServiceImpl authService) {
        return username -> {
            System.out.println("🔍 Intentando autenticar usuario: " + username);
            
            // Buscar por username O email
            java.util.Optional<ppi.e_commerce.Model.User> maybeUser = userRepository.findByUsername(username);
            if (maybeUser.isEmpty()) {
                maybeUser = userRepository.findByEmail(username);
            }

            ppi.e_commerce.Model.User appUser = maybeUser
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));

            // Verificar que el usuario esté activo
            if (!appUser.isActive()) {
                System.out.println("❌ Usuario inactivo: " + username);
                throw new org.springframework.security.authentication.DisabledException("Usuario desactivado");
            }

            // Normalizar el rol
            String rawRole = appUser.getRole();
            if (rawRole == null || rawRole.isBlank()) {
                rawRole = "USER";
            }
            
            rawRole = rawRole.trim().toUpperCase();
            if (rawRole.startsWith("ROLE_")) {
                rawRole = rawRole.substring(5);
            }
            
            String finalRole = rawRole;
            
            System.out.println("✅ Usuario encontrado: " + appUser.getUsername());
            System.out.println("📋 Role: " + finalRole);
            System.out.println("🟢 Activo: " + appUser.isActive());
            System.out.println("🔐 Usando contraseña temporal: " + appUser.isUsingTempPassword());

            // CRÍTICO: Determinar qué contraseña usar
            String passwordToUse;
            if (authService.estaUsandoContrasenaTemporal(appUser)) {
                passwordToUse = appUser.getTempPasswordHash();
                System.out.println("🔑 Usando contraseña temporal");
            } else {
                passwordToUse = appUser.getPassword();
                System.out.println("🔑 Usando contraseña permanente");
            }

            return User.withUsername(appUser.getUsername())
                .password(passwordToUse)
                .roles(finalRole)
                .disabled(!appUser.isActive())
                .build();
        };
    }

    @Bean
    public AuthenticationFailureHandler authenticationFailureHandler() {
        return (request, response, exception) -> {
            System.out.println("❌ Fallo de autenticación: " + exception.getMessage());
            
            String adminParam = request.getParameter("admin");
            boolean attemptedAdminLogin = adminParam != null && "true".equalsIgnoreCase(adminParam);

            String target;
            if (attemptedAdminLogin) {
                if (exception instanceof org.springframework.security.authentication.DisabledException) {
                    target = "/admin/login?disabled=true";
                } else {
                    target = "/admin/login?error=true";
                }
            } else {
                if (exception instanceof org.springframework.security.authentication.DisabledException) {
                    target = "/login?disabled=true";
                } else {
                    target = "/login?error=true";
                }
            }
            
            System.out.println("↪️ Redirigiendo a: " + target);
            response.sendRedirect(request.getContextPath() + target);
        };
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                
                System.out.println("\n🎉 Autenticación exitosa!");
                System.out.println("👤 Usuario: " + authentication.getName());
                
                // ⭐ VERIFICAR SI ESTÁ USANDO CONTRASEÑA TEMPORAL
                String username = authentication.getName();
                java.util.Optional<ppi.e_commerce.Model.User> maybeUser = userRepository.findByUsername(username);
                if (maybeUser.isEmpty()) {
                    maybeUser = userRepository.findByEmail(username);
                }
                
                if (maybeUser.isPresent()) {
                    ppi.e_commerce.Model.User user = maybeUser.get();
                    
                    // Si está usando contraseña temporal, FORZAR cambio de contraseña
                    if (authService.estaUsandoContrasenaTemporal(user)) {
                        System.out.println("⚠️ Contraseña temporal detectada - redirigiendo a cambio obligatorio");
                        response.sendRedirect(request.getContextPath() + "/auth/change-password?temp=true");
                        return;
                    }
                }
                
                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                System.out.println("🔑 Authorities: " + authorities);
                
                // Verificar si tiene rol ADMIN
                boolean isAdmin = authorities.stream()
                    .anyMatch(a -> {
                        String auth = a.getAuthority();
                        System.out.println("  - Verificando authority: " + auth);
                        return auth.equals("ROLE_ADMIN");
                    });

                System.out.println("🛡️ Es Admin? " + isAdmin);

                String adminParam = request.getParameter("admin");
                boolean attemptedAdminLogin = adminParam != null && "true".equalsIgnoreCase(adminParam);
                
                System.out.println("🔐 Intentó login admin? " + attemptedAdminLogin);

                String targetUrl;
                
                if (attemptedAdminLogin) {
                    // Intentó acceder al panel admin
                    if (isAdmin) {
                        targetUrl = "/admin";
                        System.out.println("✅ Acceso admin concedido → " + targetUrl);
                    } else {
                        // NO es admin pero intentó acceder al panel admin
                        System.out.println("⛔ Usuario sin permisos de admin, cerrando sesión");
                        request.getSession().invalidate();
                        response.sendRedirect(request.getContextPath() + "/admin/login?not_admin=true");
                        return;
                    }
                } else {
                    // Login normal de cliente
                    if (isAdmin) {
                        // Admin usando login de cliente → redirigir a admin
                        targetUrl = "/admin";
                        System.out.println("ℹ️ Admin detectado en login cliente → " + targetUrl);
                    } else {
                        // Usuario normal
                        targetUrl = "/products";
                        System.out.println("✅ Usuario normal → " + targetUrl);
                    }
                }
                
                System.out.println("↪️ Redirigiendo a: " + targetUrl + "\n");
                response.sendRedirect(request.getContextPath() + targetUrl);
            }
        };
    }
}