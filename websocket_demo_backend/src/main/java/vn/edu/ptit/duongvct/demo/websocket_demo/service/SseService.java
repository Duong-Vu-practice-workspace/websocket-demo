package vn.edu.ptit.duongvct.demo.websocket_demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class SseService implements MessageListener {
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
    private final RedisTemplate<String, String> redisTemplate;
    public SseEmitter handler(String clientID) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
        emitters.put(clientID, emitter);
        emitter.onCompletion(() -> emitters.remove(clientID));
        emitter.onTimeout(() -> emitters.remove(clientID));
        return emitter;
    }

    // Publish an event by sending it to Redis
    public void publishToEmitter(String clientID, String message) {
        // Publish the message in a JSON or delimited format (including the clientID)
        String payload = clientID + ":" + message;
        redisTemplate.convertAndSend("sse-channel", payload);
    }
    @Override
    public void onMessage(Message message, byte[] pattern) {
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        // Assuming payload is in format "clientID:message"
        int index = payload.indexOf(":");
        if (index > 0) {
            String clientID = payload.substring(0, index);
            String eventMessage = payload.substring(index + 1);
            SseEmitter emitter = emitters.get(clientID);
            if (emitter != null) {
                try {
                    emitter.send(SseEmitter.event().data(eventMessage));
                } catch (IOException e) {
                    emitter.completeWithError(e);
                    emitters.remove(clientID);
                }
            }
        }
    }
    public Collection<SseEmitter> getAllEmitters() {
        return emitters.values();
    }
}
