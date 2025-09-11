package vn.edu.ptit.duongvct.demo.websocket_demo.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class WsUsersLogger {
    private final SimpUserRegistry simpUserRegistry;

    @Scheduled(fixedRate = 10000)
    public void logConnectedUsers() {
        // Log detailed user information
        simpUserRegistry.getUsers().forEach(user -> {
            log.info("User: {}, sessions: {}", user.getName(),
                    user.getSessions().stream().map(s -> s.getId()).collect(Collectors.joining(", ")));

            // Log subscriptions for each session
            user.getSessions().forEach(session -> {
                session.getSubscriptions().forEach(sub -> {
                    log.info("  Session {} subscribed to: {}", session.getId(), sub.getDestination());
                });
            });
        });

        // Summary of connected users
        String users = simpUserRegistry.getUsers().stream()
                .map(SimpUser::getName)
                .collect(Collectors.joining(", "));
        log.info("WS connected users: {}", users.isEmpty() ? "(none)" : users);
    }
}