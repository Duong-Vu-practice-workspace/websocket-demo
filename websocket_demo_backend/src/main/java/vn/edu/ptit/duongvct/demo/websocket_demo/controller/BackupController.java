package vn.edu.ptit.duongvct.demo.websocket_demo.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.ActionLog;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.Backup;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.ActionLogRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.BackupRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.KafkaProducerService;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/backup")

public class BackupController {
    private final BackupRepository backupRepository;
    private final ActionLogRepository actionLogRepository;
    private final KafkaProducerService producerService;
    @Value("${kafka.backup-topic}")
    private String topicBackupCommand;

    public BackupController(BackupRepository backupRepository, ActionLogRepository actionLogRepository, KafkaProducerService producerService) {
        this.backupRepository = backupRepository;
        this.actionLogRepository = actionLogRepository;
        this.producerService = producerService;
    }

    @PostMapping
    public String createBackup(@RequestBody Backup backup) {
        backup.setStatus("CREATING");
        backupRepository.save(backup);
        ActionLog actionLog = new ActionLog();
        actionLog.setAction("Create");
        actionLog.setEntityType("Backup");
        actionLog.setStartTime(LocalDateTime.now());
        actionLog.setEndTime(LocalDateTime.now());
        actionLog.setStatus("CREATING");
        actionLogRepository.save(actionLog);
        producerService.sendMessage(topicBackupCommand, actionLog.getAction() + " " + actionLog.getEntityType() + " " + backup.getId());
        return "success";
    }
}
