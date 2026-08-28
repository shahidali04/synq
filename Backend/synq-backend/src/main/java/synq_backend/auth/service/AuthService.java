package synq_backend.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import synq_backend.auth.dto.LoginRequest;
import synq_backend.auth.dto.LoginResponse;
import synq_backend.auth.security.JwtService;
import synq_backend.common.exception.AuthenticationException;
import synq_backend.user.entity.User;
import synq_backend.user.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    //Constructor Injection
    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // Finds the user and verifies the login password.
    public LoginResponse authenticate(LoginRequest request){

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AuthenticationException("Invalid emil or password"));

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPasswordHash())){
            throw new AuthenticationException("Invalid email or password");
        }

        //Generate token during login
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = jwtService.generateRefreshToken(user.getEmail());

        return new LoginResponse(accessToken, refreshToken);
    }

}
