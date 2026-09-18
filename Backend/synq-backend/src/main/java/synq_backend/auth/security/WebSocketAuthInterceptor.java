package synq_backend.auth.security;

import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.messaging.Message;
import synq_backend.user.entity.User;
import synq_backend.user.repository.UserRepository;
import org.springframework.messaging.support.ChannelInterceptor;
import synq_backend.conversation.repository.ConversationParticipantRepository;

import java.util.Collections;
import java.util.UUID;

// Authenticates users when they establish a STOMP WebSocket connection.
@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ConversationParticipantRepository participantRepository;

    public WebSocketAuthInterceptor(
            JwtService jwtService,
            UserRepository userRepository,
            ConversationParticipantRepository participantRepository
    ){
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.participantRepository = participantRepository;
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

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {

            String destination = accessor.getDestination();

            if (destination != null &&
                    destination.startsWith("/topic/conversation/")) {

                String conversationIdString =
                        destination.substring("/topic/conversation/".length());

                try {
                    UUID conversationId = UUID.fromString(conversationIdString);

                    if (accessor.getUser() == null) {
                        throw new AccessDeniedException(
                                "Unauthenticated WebSocket subscription"
                        );
                    }

                    UUID currentUserId =
                            UUID.fromString(accessor.getUser().getName());

                    boolean isParticipant =
                            participantRepository.existsByConversationIdAndUserId(
                                    conversationId,
                                    currentUserId
                            );

                    if (!isParticipant) {
                        throw new AccessDeniedException(
                                "User is not a participant of this conversation"
                        );
                    }

                } catch (IllegalArgumentException e) {
                    throw new AccessDeniedException(
                            "Invalid conversation subscription"
                    );
                }
            }
        }

        return message;
    }
}
