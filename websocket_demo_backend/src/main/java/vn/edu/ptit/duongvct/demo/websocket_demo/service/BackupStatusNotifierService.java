// ...existing code...
package vn.edu.ptit.duongvct.demo.websocket_demo.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.Backup;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupStatusNotifierService {
    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    public void publishBackupStatus(Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            messagingTemplate.convertAndSend("/topic/backup-status", json);
            messagingTemplate.convertAndSend("/queue/duongvct_queue_executor", json);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize payload: {}", e.getMessage());
            messagingTemplate.convertAndSend("/topic/backup-status", payload);
            messagingTemplate.convertAndSend("/queue/duongvct_queue_executor", payload);
        }
    }

    public void publishBackupStatusToUser(String username, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            log.info("Sending WS message to /queue/user-{}: {}", username, json);
            messagingTemplate.convertAndSend("/queue/user-" + username, json);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize payload for user {}: {}", username, e.getMessage());
            messagingTemplate.convertAndSend("/queue/user-" + username, payload);
        }
    }

}