package synq_backend.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.messaging.simp.config.ChannelRegistration;
import synq_backend.auth.security.WebSocketAuthInterceptor;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    public WebSocketConfig(
            WebSocketAuthInterceptor webSocketAuthInterceptor
    ){
        this.webSocketAuthInterceptor = webSocketAuthInterceptor;
    }

    // Configure message routing for WebSocket communication.
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry){

        // Used for messages sent from the server to subscribed clients.
        registry.enableSimpleBroker("/topic");

        // Used for messages sent from clients to the server.
        registry.setApplicationDestinationPrefixes("/app");
    }

    //Configure the endpoint through which clients establish WebSocket connection.
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){

        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*");
    }

    // Registers the authentication interceptor for incoming STOMP messages.
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }

}
