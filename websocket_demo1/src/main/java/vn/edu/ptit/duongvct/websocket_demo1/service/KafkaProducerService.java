package vn.edu.ptit.duongvct.websocket_demo1.service;

import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@AllArgsConstructor
public class KafkaProducerService {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(String topic, String message) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message).toCompletableFuture();
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                throw new RuntimeException(ex);
            }
        });
    }
}
