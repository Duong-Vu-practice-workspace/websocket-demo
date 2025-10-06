package vn.edu.ptit.duongvct.websocket_demo1.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.edu.ptit.duongvct.websocket_demo1.domain.ActionLog;
import vn.edu.ptit.duongvct.websocket_demo1.domain.Backup;
import vn.edu.ptit.duongvct.websocket_demo1.repository.ActionLogRepository;
import vn.edu.ptit.duongvct.websocket_demo1.repository.BackupRepository;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaListenerService {
    private final BackupRepository backupRepository;
    private final ActionLogRepository actionLogRepository;
    private final KafkaProducerService kafkaProducerService;
    @Value("${kafka.backup-topic}")
    private String topicBackupCommand;
    @Value("${kafka.notifier-topic}")
    private String notifierTopicCommand;


    @KafkaListener(topics = "${kafka.backup-topic}", groupId = "${spring.kafka.consumer.group-id}")
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
    private void createBackup(String id, String clientName) throws InterruptedException {
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
            String message = actionLog.getAction() + " " + actionLog.getEntityType() + " " + actionLog.getEntityId() + " " + clientName;
            kafkaProducerService.sendMessage(notifierTopicCommand, message);


        }

    }

}
