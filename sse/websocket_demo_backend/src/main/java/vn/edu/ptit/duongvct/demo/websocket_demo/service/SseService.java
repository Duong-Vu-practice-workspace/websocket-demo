package vn.edu.ptit.duongvct.demo.websocket_demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class SseService implements MessageListener {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public SseEmitter handler(String clientID) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(clientID, emitter);
        emitter.onCompletion(() -> emitters.remove(clientID));
        emitter.onTimeout(() -> emitters.remove(clientID));
        emitter.onError((e) -> emitters.remove(clientID));
        return emitter;
    }
    public void publishToEmitter(String clientID, Object payload) {
        try {
            JsonNode root = objectMapper.createObjectNode()
                    .put("clientId", clientID)
                    .set("body", objectMapper.valueToTree(payload));
            String json = objectMapper.writeValueAsString(root);
            // publish to redis channel
            redisTemplate.convertAndSend("sse-channel", json);
        } catch (Exception e) {
            log.error("Error happen: {}", e.getMessage());
        }

    }
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            JsonNode root = objectMapper.readTree(payload);
            String clientId = root.path("clientId").asText(null);
            JsonNode bodyNode = root.path("body");
            if (clientId != null && !bodyNode.isMissingNode()) {
                SseEmitter emitter = emitters.get(clientId);
                if (emitter != null) {
                    String bodyJson = objectMapper.writeValueAsString(bodyNode);
                    emitter.send(SseEmitter.event().name("sse-event").data(bodyJson, MediaType.APPLICATION_JSON));
                }
            }
        } catch (Exception e) {
            log.error("Error happening while receiving message: {}", e.getMessage());
        }
    }
    public Collection<SseEmitter> getAllEmitters() {
        return emitters.values();
    }
}
