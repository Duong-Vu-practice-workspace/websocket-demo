package vn.edu.ptit.duongvct.demo.websocket_demo.configuration.handler;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Collections;
import java.util.Map;

@Component
public class PrincipleHandshakeHandler extends DefaultHandshakeHandler {
    @Autowired
    private JwtDecoder jwtDecoder;
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Jwt jwt = jwtDecoder.decode(token);
                String username = jwt.getSubject();
                return new UsernamePasswordAuthenticationToken(username, null, Collections.emptyList());
            } catch (Exception e) {
                // Log error if needed
            }
        }
        return null;  // Anonymous if no token
    }
}