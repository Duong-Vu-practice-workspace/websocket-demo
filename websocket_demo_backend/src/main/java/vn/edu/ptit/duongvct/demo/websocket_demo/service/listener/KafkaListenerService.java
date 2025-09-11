package vn.edu.ptit.duongvct.demo.websocket_demo.service.listener;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.Backup;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.ActionLogRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.BackupRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.BackupStatusNotifierService;

import java.util.*;

@Service
@Slf4j

public class KafkaListenerService {
    private final BackupRepository backupRepository;
    private final ActionLogRepository actionLogRepository;
    private final BackupStatusNotifierService notifier;

    @Value("${kafka.backup-topic}")
    private String topicBackupCommand;

    public KafkaListenerService(BackupRepository backupRepository, ActionLogRepository actionLogRepository, BackupStatusNotifierService notifier) {
        this.backupRepository = backupRepository;
        this.actionLogRepository = actionLogRepository;
        this.notifier = notifier;
    }

    @KafkaListener(topics = "${kafka.backup-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void executeBackupCommand(String message) {
        List<String> list = Arrays.stream(message.split(" ")).toList();
        String extractMessage = list.get(0) + " " + list.get(1);
        switch (extractMessage) {
            case "Create Backup":
                createBackup(list.getLast());
                break;
        }
    }
    private void createBackup(String id) {
        Long convertId = Long.parseLong(id);
        Optional<Backup> backupOptional = backupRepository.findById(convertId);
        if (backupOptional.isPresent()) {
            Backup backup = backupOptional.get();
            backup.setStatus("COMPLETED");
            Backup saved = backupRepository.save(backup);
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", saved.getId());
            payload.put("status", saved.getStatus());
            String username = saved.getUser() != null ? saved.getUser().getUsername() : null;
            if (username != null) {
                notifier.publishBackupStatusToUser(username, payload);
                log.info("Backup {} updated -> notified user {}", saved.getId(), username);
            } else {
                notifier.publishBackupStatus(payload);
                log.warn("Backup {} has no associated user, broadcasting", saved.getId());
            }
            log.info("Backup {} updated -> notified frontend", saved.getId());
        }

    }

}
