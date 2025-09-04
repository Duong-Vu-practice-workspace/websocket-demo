package vn.edu.ptit.duongvct.demo.websocket_demo.configuration;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String QUEUE_NAME = "duongvct_queue_executor";
    @Bean
    public Queue queue(){
        return new Queue(QUEUE_NAME, true, false, false);
    }
}
