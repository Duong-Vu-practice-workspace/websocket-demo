package vn.edu.ptit.duongvct.demo.websocket_demo.controller;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.ActionLog;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.Backup;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.User;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.ActionLogRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.BackupRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.UserRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.KafkaProducerService;
import vn.edu.ptit.duongvct.demo.websocket_demo.util.SecurityUtil;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/backup")

public class BackupController {
    private final BackupRepository backupRepository;
    private final ActionLogRepository actionLogRepository;
    private final KafkaProducerService producerService;
    private final UserRepository userRepository;
    @Value("${kafka.backup-topic}")
    private String topicBackupCommand;

    public BackupController(BackupRepository backupRepository, ActionLogRepository actionLogRepository, KafkaProducerService producerService, UserRepository userRepository) {
        this.backupRepository = backupRepository;
        this.actionLogRepository = actionLogRepository;
        this.producerService = producerService;
        this.userRepository = userRepository;
    }
    @PostMapping
    public String createBackup(@RequestBody Backup backup) {
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
        actionLogRepository.save(actionLog);
        producerService.sendMessage(topicBackupCommand, actionLog.getAction() + " " + actionLog.getEntityType() + " " + backup.getId());
        return String.valueOf(saved.getId());
    }

    @GetMapping("/{id}")
    public Backup getBackupById(@PathVariable Long id) {
        return backupRepository.findById(id).orElse(null);
    }
}
