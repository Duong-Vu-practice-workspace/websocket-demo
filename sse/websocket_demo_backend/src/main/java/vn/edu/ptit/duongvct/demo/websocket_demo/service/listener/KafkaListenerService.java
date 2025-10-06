package vn.edu.ptit.duongvct.demo.websocket_demo.service.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.ActionLog;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.Backup;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.ActionLogRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.BackupRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.BackupStatusNotifierService;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.SseService;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaListenerService {
    private final BackupRepository backupRepository;
    private final BackupStatusNotifierService notifier;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5); // Pool for async tasks
    private final SseService sseService;
    @Value("${kafka.backup-topic}")
    private String topicBackupCommand;

    @KafkaListener(topics = "${kafka.notifier-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void executeBackupCommand(String message) throws InterruptedException {
        List<String> list = Arrays.stream(message.split(" ")).toList();
        String extractMessage = list.get(0) + " " + list.get(1);
        String backupId = list.get(2);
        String clientName = list.getLast();
        switch (extractMessage) {
            case "Create Backup":
                createBackup(backupId, clientName);
                break;
        }
    }
    private void createBackup(String id, String clientId) {
        Long convertId = Long.parseLong(id);
        Optional<Backup> backupOptional = backupRepository.findById(convertId);
        if (backupOptional.isPresent()) {
            Backup saved = backupOptional.get();
            saved.setUser(null);
            sseService.publishToEmitter(clientId, saved);
        }
    }
}
