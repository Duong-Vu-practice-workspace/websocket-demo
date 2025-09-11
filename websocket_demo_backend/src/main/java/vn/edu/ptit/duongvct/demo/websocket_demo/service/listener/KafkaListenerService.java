package vn.edu.ptit.duongvct.demo.websocket_demo.service.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.ActionLog;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.Backup;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.ActionLogRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.BackupRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.BackupStatusNotifierService;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j

public class KafkaListenerService {
    private final BackupRepository backupRepository;
    private final ActionLogRepository actionLogRepository;
    private final BackupStatusNotifierService notifier;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5); // Pool for async tasks

    @Value("${kafka.backup-topic}")
    private String topicBackupCommand;

    public KafkaListenerService(BackupRepository backupRepository, ActionLogRepository actionLogRepository, BackupStatusNotifierService notifier) {
        this.backupRepository = backupRepository;
        this.actionLogRepository = actionLogRepository;
        this.notifier = notifier;
    }

    @KafkaListener(topics = "${kafka.backup-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void executeBackupCommand(String message) throws InterruptedException {
        List<String> list = Arrays.stream(message.split(" ")).toList();
        String extractMessage = list.get(0) + " " + list.get(1);
        switch (extractMessage) {
            case "Create Backup":
                createBackup(list.getLast());
                break;
        }
    }
    private void createBackup(String id) throws InterruptedException {
        Long convertId = Long.parseLong(id);
        Optional<Backup> backupOptional = backupRepository.findById(convertId);
        if (backupOptional.isPresent()) {
            Backup backup = backupOptional.get();
            backup.setStatus("COMPLETED");
            Backup saved = backupRepository.save(backup);
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", saved.getId());
            payload.put("status", saved.getStatus());
            Optional<ActionLog> actionLogOptional = actionLogRepository.findByEntityId(id);
            ActionLog actionLog = actionLogOptional.get();
            actionLog.setStatus("COMPLETED");
            this.actionLogRepository.save(actionLog);
            scheduler.schedule(() -> {
                try {
                    String username = saved.getUser() != null ? saved.getUser().getUsername() : null;
                    if (username != null) {
                        notifier.publishBackupStatusToUser(username, payload);
                        log.info("Backup {} updated -> notified user {}", saved.getId(), username);
                    } else {
                        notifier.publishBackupStatus(payload);
                        log.warn("Backup {} has no associated user, broadcasting", saved.getId());
                    }
                    log.info("Backup {} updated -> notified frontend", saved.getId());
                } catch (Exception e) {
                    log.error("Error notifying backup status for {}: {}", saved.getId(), e.getMessage());
                }
            }, 2, TimeUnit.SECONDS);
            log.info("Backup {} updated -> notified frontend", saved.getId());
        }

    }

}
