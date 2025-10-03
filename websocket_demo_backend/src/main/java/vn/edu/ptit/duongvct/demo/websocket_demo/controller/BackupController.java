package vn.edu.ptit.duongvct.demo.websocket_demo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.ActionLog;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.Backup;
import vn.edu.ptit.duongvct.demo.websocket_demo.domain.User;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.ActionLogRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.BackupRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.repository.UserRepository;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.BackupStatusNotifierService;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.KafkaProducerService;
import vn.edu.ptit.duongvct.demo.websocket_demo.service.SseService;
import vn.edu.ptit.duongvct.demo.websocket_demo.util.SecurityUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Date;

@RestController
@RequestMapping("/api/v1/backup")
@RequiredArgsConstructor
public class BackupController {
    private final BackupRepository backupRepository;
    private final ActionLogRepository actionLogRepository;
    private final KafkaProducerService producerService;
    private final UserRepository userRepository;
    private final BackupStatusNotifierService backupStatusNotifierService;
    private final SseService sseService;
    @Value("${kafka.backup-topic}")
    private String topicBackupCommand;

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

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter streamEvents(@RequestParam(name = "clientID") String clientID) {
        return sseService.handler(clientID);
    }

    @GetMapping(value = "/publish")
    public String publish(@RequestParam(name = "clientID") String clientID) {
        long timestamp = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String formattedTime = sdf.format(new Date(timestamp));
        sseService.publishToEmitter(clientID, "Hello at timestamp " + formattedTime);
        return "Published to emitter: " + clientID + " at: " + formattedTime;
    }
}
