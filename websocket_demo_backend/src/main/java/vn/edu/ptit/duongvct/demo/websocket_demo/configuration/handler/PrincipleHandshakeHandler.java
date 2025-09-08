package vn.edu.ptit.duongvct.demo.websocket_demo.configuration.handler;

import org.springframework.stereotype.Component;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

@Component
public class PrincipleHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // prefer principal provided in attributes (if any)
        if (attributes != null) {
            Object attr = attributes.get("principal");
            if (attr instanceof Principal) {
                return (Principal) attr;
            }
        }

        // fallback to any principal available from the HTTP request
        Principal requestPrincipal = request.getPrincipal();
        if (requestPrincipal != null) {
            return requestPrincipal;
        }

        // default behavior
        return super.determineUser(request, wsHandler, attributes);
    }
}