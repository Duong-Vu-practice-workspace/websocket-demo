package vn.edu.ptit.duongvct.demo.websocket_demo.service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.Backup;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.BackupRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.BackupStatusNotifierService;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.SseService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaListenerService {

    private final BackupRepository backupRepository;
    private final BackupStatusNotifierService notifier;
    private final SseService sseService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);

    @Value("${kafka.backup-topic}")
    private String topicBackupCommand;

    @KafkaListener(topics = "${kafka.notifier-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void executeBackupCommand(String message) {
        List<String> list = Arrays.stream(message.split(" ")).toList();
        if (list.size() < 4) {
            log.warn("Received invalid message: {}", message);
            return;
        }
        String extractMessage = list.get(0) + " " + list.get(1);
        String backupId = list.get(2);
        String clientName = list.getLast();

        if ("Create Backup".equals(extractMessage)) {
            createBackup(backupId, clientName);
        } else {
            log.warn("Unhandled message type: {}", extractMessage);
        }
    }

    private void createBackup(String id, String clientId) {
        Long convertId = Long.parseLong(id);
        Optional<Backup> backupOptional = backupRepository.findById(convertId);

        if (backupOptional.isEmpty()) {
            log.warn("Backup {} not found for client {}", id, clientId);
            return;
        }

        Backup saved = backupOptional.get();
        saved.setUser(null);

        scheduler.schedule(() -> {
            try {
                log.info("Publishing backup {} to client {} after 2s delay", id, clientId);
                sseService.publishToEmitter(clientId, saved);
            } catch (Exception e) {
                log.error("Failed to publish backup {} to client {}", id, clientId, e);
            }
        }, 2, TimeUnit.SECONDS);
    }
}