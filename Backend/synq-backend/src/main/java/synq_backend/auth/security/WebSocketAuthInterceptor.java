package synq_backend.auth.security;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.messaging.Message;
import synq_backend.user.entity.User;
import synq_backend.user.repository.UserRepository;
import org.springframework.messaging.support.ChannelInterceptor;

import java.util.Collections;

// Authenticates users when they establish a STOMP WebSocket connection.
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public WebSocketAuthInterceptor(
            JwtService jwtService,
            UserRepository userRepository
    ){
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    public Message<?> preSend(
            Message<?> message,
            MessageChannel channel
    ) {

        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())){
            String authorization = accessor.getFirstNativeHeader("Authorization");

            if (authorization != null && authorization.startsWith("Bearer ")){

                String jwt = authorization.substring(7);

                try{
                    String email = jwtService.extractEmail(jwt);

                    if (jwtService.isTokenValid(jwt, email)) {

                        User user = userRepository.findByEmail(email)
                                .orElse(null);

                        if (user != null) {

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(
                                            user.getId().toString(),
                                            null,
                                            Collections.emptyList()
                                    );

                            accessor.setUser(authentication);
                        }
                    }

                }catch (Exception e){
                    System.out.println("Invalid WebSocket JWT token: " + e.getMessage());
                }
            }
        }

        return message;
    }
}
