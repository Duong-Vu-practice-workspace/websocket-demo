package vn.edu.ptit.duongvct.demo.websocket_demo.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class WsUsersLogger {
    private final SimpUserRegistry userRegistry;

    @Scheduled(fixedDelay = 10000)
    public void logUsers() {
        String info = userRegistry.getUsers().stream()
                .map(SimpUser::getName)
                .sorted()
                .reduce((a,b)->a + ", " + b).orElse("(none)");
        log.info("WS connected users: {}", info);
    }
}