package com.yupi.moonBI.bimqConfig;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
@Configuration
public class RabbitMQConfig {

    @Value("${mq.exchange.name}")
    private String exchangeName;

    @Value("${mq.queue.name}")
    private String queueName;

    @Value("${mq.routing.key}")
    private String routingKey;

    @Value("${mq.dead-letter.exchange}")
    private String deadLetterExchange;

    @Value("${mq.dead-letter.queue}")
    private String deadLetterQueueName;

    @Value("${mq.dead-letter.routing-key}")
    private String deadLetterRoutingKey;

    @Bean
    DirectExchange deadLetterExchange() {
        return new DirectExchange(deadLetterExchange);
    }

    @Bean
    Queue deadLetterQueue() {
        return QueueBuilder.durable(deadLetterQueueName).build();
    }

    @Bean
    Binding deadLetterBinding() {
        return BindingBuilder.bind(deadLetterQueue()).to(deadLetterExchange()).with(deadLetterRoutingKey);
    }

    @Bean
    Queue workQueue() {
        return QueueBuilder.durable(queueName)
                .withArgument("x-dead-letter-exchange", deadLetterExchange)
                .withArgument("x-dead-letter-routing-key", deadLetterRoutingKey)
                .build();
    }
}
