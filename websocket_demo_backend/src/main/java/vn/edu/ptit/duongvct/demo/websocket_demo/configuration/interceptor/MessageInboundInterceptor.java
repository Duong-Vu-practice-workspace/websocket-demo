package vn.edu.ptit.duongvct.demo.websocket_demo.configuration.interceptor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageInboundInterceptor implements ChannelInterceptor {

    private final JwtDecoder jwtDecoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        StompCommand command = accessor.getCommand();
        log.info("Command: {}", command);
        if (command == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(command)) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (!StringUtils.hasText(authHeader)) {
                authHeader = accessor.getFirstNativeHeader("authorization");
            }
            if (!StringUtils.hasText(authHeader)) {
                authHeader = accessor.getFirstNativeHeader("token");
            }

            if (!StringUtils.hasText(authHeader)) {
                log.warn("WebSocket CONNECT rejected: missing Authorization header");
                return null; // reject connect
            }

            String token = authHeader;
            if (token.startsWith("Bearer ")) {
                token = token.substring(7).trim();
            }

            if (!StringUtils.hasText(token)) {
                log.warn("WebSocket CONNECT rejected: empty token after Bearer");
                return null;
            }

            try {
                Jwt jwt = jwtDecoder.decode(token);
                Authentication auth = new JwtAuthenticationToken(jwt, Collections.emptyList());

                // set security context and persist principal on the accessor and session attributes
                SecurityContextHolder.getContext().setAuthentication(auth);
                accessor.setUser(auth);

                Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
                if (sessionAttrs != null) {
                    sessionAttrs.put("principal", auth);
                    // some clients/frameworks expect "simpUser"
                    sessionAttrs.put("simpUser", auth);
                }

                log.info("set User : {}", auth);
                return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
            } catch (JwtException ex) {
                log.warn("WebSocket CONNECT rejected: invalid JWT: {}", ex.getMessage());
                return null;
            }
        }

        // For SUBSCRIBE and SEND ensure an authenticated principal is available.
        if (StompCommand.SUBSCRIBE.equals(command) || StompCommand.SEND.equals(command)) {
            Authentication frameAuth = null;

            if (accessor.getUser() instanceof Authentication) {
                frameAuth = (Authentication) accessor.getUser();
            } else {
                Map<String, Object> sessionAttrs = accessor.getSessionAttributes();
                if (sessionAttrs != null) {
                    Object attr = sessionAttrs.get("principal");
                    if (attr instanceof Authentication) {
                        frameAuth = (Authentication) attr;
                    } else {
                        Object simpUser = sessionAttrs.get("simpUser");
                        if (simpUser instanceof Authentication) {
                            frameAuth = (Authentication) simpUser;
                        }
                    }
                }
            }

            if (frameAuth == null) {
                Authentication scAuth = SecurityContextHolder.getContext().getAuthentication();
                if (scAuth instanceof Authentication) {
                    frameAuth = scAuth;
                }
            }

            if (frameAuth == null || !frameAuth.isAuthenticated()) {
                log.warn("WebSocket {} rejected: no authenticated Principal", command);
                return null;
            }

            // ensure accessor has the user for downstream handlers
            accessor.setUser(frameAuth);
            return MessageBuilder.createMessage(message.getPayload(), accessor.getMessageHeaders());
        }

        if (StompCommand.DISCONNECT.equals(command)) {
            // clear thread SecurityContext when client disconnects
            SecurityContextHolder.clearContext();
        }

        return message;
    }
}