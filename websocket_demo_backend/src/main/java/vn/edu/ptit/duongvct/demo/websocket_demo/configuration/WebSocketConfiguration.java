package vn.edu.ptit.duongvct.demo.websocket_demo.configuration;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import vn.edu.ptit.duongvct.demo.websocket_demo.configuration.handler.PrincipleHandshakeHandler;
import vn.edu.ptit.duongvct.demo.websocket_demo.configuration.interceptor.MessageInboundInterceptor;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketConfiguration implements WebSocketMessageBrokerConfigurer {
    @Value("${cors.allow-origins}")
    private String allowOriginConfig;

    private final MessageInboundInterceptor messageInboundInterceptor;
    // RabbitMQ STOMP relay settings (Rabbit STOMP plugin default port 61613)
    @Value("${spring.rabbitmq.relayHost:rabbitmq}")
    private String rabbitHost;

    @Value("${spring.rabbitmq.relayPort:61613}")
    private int rabbitStompPort; // override if you use a different STOMP port

    @Value("${spring.rabbitmq.username:guest}")
    private String rabbitUser;

    @Value("${spring.rabbitmq.password:guest}")
    private String rabbitPass;

    private final PrincipleHandshakeHandler principleHandshakeHandler;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        var origins = allowOriginConfig.split(",");
        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins)
                .setHandshakeHandler(principleHandshakeHandler());
        registry.addEndpoint("/ws")
                .setAllowedOrigins(origins)
                .setHandshakeHandler(principleHandshakeHandler())
                .withSockJS(); // keep SockJS for browser clients; remove if not needed
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Application destination prefix for messages handled by @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");

        // User destination prefix (for point-to-point)
        registry.setUserDestinationPrefix("/user");

        // Relay to external STOMP broker (RabbitMQ STOMP plugin)
        registry.enableStompBrokerRelay("/topic", "/queue", "/exchange")
                .setRelayHost(rabbitHost)
                .setRelayPort(rabbitStompPort)
                .setClientLogin(rabbitUser)
                .setClientPasscode(rabbitPass)
                .setSystemLogin(rabbitUser)
                .setSystemPasscode(rabbitPass)
                // heartbeat values (ms) — adjust if needed
                .setSystemHeartbeatSendInterval(10000)
                .setSystemHeartbeatReceiveInterval(10000);
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(messageInboundInterceptor);
    }

    // keep this method to avoid field access in lambda and to make handshake handler explicit
    private PrincipleHandshakeHandler principleHandshakeHandler() {
        return this.principleHandshakeHandler;
    }
}
