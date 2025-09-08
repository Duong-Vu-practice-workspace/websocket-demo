// ...existing code...
package vn.edu.ptit.duongvct.demo.websocket_demo.service;

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

    public void notifyBackupUpdated(Backup backup) {
        if (backup == null || backup.getId() == null) return;
        String destination = "/topic/backup/" + backup.getId();
        try {
            messagingTemplate.convertAndSend(destination, backup);
            log.info("Sent websocket update to {} for backup id={}", destination, backup.getId());
        } catch (Exception e) {
            log.warn("Failed to send websocket update for backup {}: {}", backup.getId(), e.getMessage());
        }
    }
}