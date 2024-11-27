package Erik.OnlineSong.Config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Enables WebSocket message handling, backed by a message broker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic"); // Enables a simple in-memory message broker that broadcasts messages to
                                             // subscribed clients on the "/topic" destination
        config.setApplicationDestinationPrefixes("/app"); // Sets the prefix for messages that are routed to the
                                                          // application (e.g., for sending messages from the client)
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Registers STOMP endpoints
        registry.addEndpoint("/ws") // Defines the WebSocket endpoint for clients to connect to
                .setAllowedOriginPatterns("*") // Allows connections from any origin (CORS configuration)
                .withSockJS(); // Enables SockJS fallback options for browsers that don't support WebSockets
    }
}