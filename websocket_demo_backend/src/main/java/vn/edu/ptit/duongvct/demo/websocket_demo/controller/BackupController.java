package vn.edu.ptit.duongvct.demo.websocket_demo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.ActionLog;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.Backup;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.User;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.ActionLogRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.BackupRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.UserRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.BackupStatusNotifierService;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.KafkaProducerService;
import vn.edu.ptit.duongvct.demo.websocket_demo.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/backup")

public class BackupController {
    private final BackupRepository backupRepository;
    private final ActionLogRepository actionLogRepository;
    private final KafkaProducerService producerService;
    private final UserRepository userRepository;
    private final BackupStatusNotifierService backupStatusNotifierService;
    @Value("${kafka.backup-topic}")
    private String topicBackupCommand;

    public BackupController(BackupRepository backupRepository, ActionLogRepository actionLogRepository, KafkaProducerService producerService, UserRepository userRepository, BackupStatusNotifierService backupStatusNotifierService) {
        this.backupRepository = backupRepository;
        this.actionLogRepository = actionLogRepository;
        this.producerService = producerService;
        this.userRepository = userRepository;
        this.backupStatusNotifierService = backupStatusNotifierService;
    }
    @PostMapping
    public Backup createBackup(@RequestBody Backup backup) {
        User user = null;
        String username = SecurityUtil.getCurrentUserLogin().orElse(null);
        if (username != null) {
            user = userRepository.findByUsername(username).orElse(null);
        }
//        if (jwt != null) {
//            String username = jwt.getSubject();  // Subject is the email (from JWT)
//            user = userRepository.findByUsername(username).orElse(null);
//        }
        backup.setUser(user);
        backup.setStatus("CREATING");
        Backup saved = backupRepository.save(backup);
        ActionLog actionLog = new ActionLog();
        actionLog.setAction("Create");
        actionLog.setEntityType("Backup");
        actionLog.setStartTime(LocalDateTime.now());
        actionLog.setEndTime(LocalDateTime.now());
        actionLog.setStatus("CREATING");
        actionLog.setEntityId(String.valueOf(saved.getId()));
        actionLogRepository.save(actionLog);
        producerService.sendMessage(topicBackupCommand, actionLog.getAction() + " " + actionLog.getEntityType() + " " + backup.getId());
        saved.setUser(null);
        return saved;
    }

    @GetMapping("/{id}")
    public Backup getBackupById(@PathVariable Long id) {
        return backupRepository.findById(id).orElse(null);
    }
    @PostMapping("/test-broadcast")
    public ResponseEntity<?> testBroadcast() {
        backupStatusNotifierService.publishBackupStatus(Map.of("test", "broadcast"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test-user/{username}")
    public ResponseEntity<?> testUserSpecific(@PathVariable String username) {
        backupStatusNotifierService.publishBackupStatusToUser(username, Map.of("test", "user-specific"));
        return ResponseEntity.ok().build();
    }
}
