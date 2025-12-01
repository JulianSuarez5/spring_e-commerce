package ppi.e_commerce.Controller.Api;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ppi.e_commerce.Dto.AuthRequest;
import ppi.e_commerce.Dto.AuthResponse;
import ppi.e_commerce.Dto.RefreshTokenRequest;
import ppi.e_commerce.Model.User;
import ppi.e_commerce.Repository.UserRepository;
import ppi.e_commerce.Utils.JwtUtil;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthApiController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest authRequest) {
        try {
            // Autenticar usuario
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(),
                    authRequest.getPassword()
                )
            );

            // Obtener usuario de la base de datos
            Optional<User> userOpt = userRepository.findByUsername(authRequest.getUsername());
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByEmail(authRequest.getUsername());
            }

            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Usuario no encontrado");
            }

            User user = userOpt.get();

            // Generar tokens JWT
            String role = user.getRole() != null ? user.getRole() : "USER";
            if (role.startsWith("ROLE_")) {
                role = role.substring(5);
            }
            
            String accessToken = jwtUtil.generateToken(user.getUsername(), role);
            String refreshToken = jwtUtil.generateRefreshToken(user.getUsername());

            // Crear respuesta
            AuthResponse response = new AuthResponse(
                accessToken,
                refreshToken,
                user.getUsername(),
                role,
                user.getId(),
                user.getEmail(),
                user.getName()
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Credenciales inválidas");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Error al autenticar: " + e.getMessage());
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            String refreshToken = request.getRefreshToken();
            String username = jwtUtil.extractUsername(refreshToken);
            
            if (jwtUtil.validateToken(refreshToken, username)) {
                Optional<User> userOpt = userRepository.findByUsername(username);
                if (userOpt.isPresent()) {
                    User user = userOpt.get();
                    String role = user.getRole() != null ? user.getRole() : "USER";
                    if (role.startsWith("ROLE_")) {
                        role = role.substring(5);
                    }
                    
                    String newAccessToken = jwtUtil.generateToken(user.getUsername(), role);
                    String newRefreshToken = jwtUtil.generateRefreshToken(user.getUsername());
                    
                    AuthResponse response = new AuthResponse(
                        newAccessToken,
                        newRefreshToken,
                        user.getUsername(),
                        role,
                        user.getId(),
                        user.getEmail(),
                        user.getName()
                    );
                    return ResponseEntity.ok(response);
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token inválido");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token inválido");
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwtToken = token.substring(7);
                String username = jwtUtil.extractUsername(jwtToken);
                
                if (jwtUtil.validateToken(jwtToken, username)) {
                    Optional<User> userOpt = userRepository.findByUsername(username);
                    if (userOpt.isPresent()) {
                        User user = userOpt.get();
                        String role = user.getRole() != null ? user.getRole() : "USER";
                        if (role.startsWith("ROLE_")) {
                            role = role.substring(5);
                        }
                        
                        AuthResponse response = new AuthResponse(
                            jwtToken,
                            null, // No se devuelve refresh token en validate
                            user.getUsername(),
                            role,
                            user.getId(),
                            user.getEmail(),
                            user.getName()
                        );
                        return ResponseEntity.ok(response);
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token inválido");
        }
    }
}

