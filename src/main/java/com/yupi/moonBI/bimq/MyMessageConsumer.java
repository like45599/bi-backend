package com.yupi.moonBI.bimq;

import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
@Slf4j
public class MyMessageConsumer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 接收消息的方法
     *
     * @param message
     * @param channel
     * @param deliveryTag
     */
    //使用@SneakyThrows注解简化异常处理
    @SneakyThrows
    //使用该注解指定程序要监听的队列，，并设置消息的确认机制为手动
    @RabbitListener(queues = {"code_queue"}, ackMode = "MANUAL")
    //@Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag 用于从消息头中获取投递标签deliveryTag
    //在mq中，每条消息都会被分配一个唯一投递标签，用于标识该消息在通道中的投递状态和顺序，使用该注解可以从消息头中获取该投递标签，并将其赋值给deliveryTag参数，
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message={}", message);
        //手动确认消息，消息确认标志设置为false，消息才能被确认
        channel.basicAck(deliveryTag, false);
    }
}
