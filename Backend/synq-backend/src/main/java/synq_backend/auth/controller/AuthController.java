package synq_backend.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import synq_backend.auth.dto.LoginRequest;
import synq_backend.auth.dto.LoginResponse;
import synq_backend.auth.dto.RefreshTokenRequest;
import synq_backend.auth.security.JwtService;
import synq_backend.auth.service.AuthService;
import synq_backend.user.entity.User;
import synq_backend.user.repository.UserRepository;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    // Constructor Injection
    public AuthController(AuthService authService,
                          JwtService jwtService,
                          UserRepository userRepository){
        this.authService = authService;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    // Authenticates a user with email and password.
    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request){
        return authService.authenticate(request);
    }

    // Refreshes the access token without requiring the user to log in again.
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenRequest request){

        try{
            String refreshToken = request.getRefreshToken();

            String email = jwtService.extractEmail(refreshToken);

            User user = userRepository.findByEmail(email)
                    .orElse(null);

            if (user == null){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            if (!jwtService.isRefreshTokenValid(refreshToken, email)){
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            String newAccessToken = jwtService.generateAccessToken(email);

            return ResponseEntity.ok(
                    new LoginResponse(newAccessToken, refreshToken)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
