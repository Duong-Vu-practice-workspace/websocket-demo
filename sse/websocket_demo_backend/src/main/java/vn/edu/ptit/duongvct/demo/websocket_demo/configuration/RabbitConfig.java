package vn.edu.ptit.duongvct.demo.websocket_demo.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
@Slf4j
@Configuration
public class RabbitConfig {
    public static final String QUEUE_NAME = "duongvct_queue_executor";
    @Bean
    public Queue queue(){
        return new Queue(QUEUE_NAME, true, false, false);
    }

    @Bean
    public AmqpAdmin amqpAdmin(ConnectionFactory connectionFactory) {
        return new RabbitAdmin(connectionFactory);
    }
    @Bean
    public Binding bindBackupStatusQueue(Queue queue) {
        // Bind to built-in amq.topic exchange with routing key "backup-status"
        return new Binding(queue.getName(), Binding.DestinationType.QUEUE, "amq.topic", "backup-status", null);
    }

}
