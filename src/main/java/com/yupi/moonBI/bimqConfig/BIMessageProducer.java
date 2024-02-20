package com.yupi.moonBI.bimqConfig;

import com.yupi.moonBI.MQConstant.BiMqConstant;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class BIMessageProducer {

    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param message
     */
    public void sendMessage(String message) {
        rabbitTemplate.convertAndSend(BiMqConstant.BI_EXCHANGE_NAME, BiMqConstant.BI_ROUTING_KEY, message);
        System.out.println(" BIMessageProducer Message sent: " + message);
    }

//    /**
//     * @param chartTask 消息内容对象
//     */
//    public void sendMessage(ChartTask chartTask) {
//        // 使用convertAndSend方法将消息发送到指定的交换机和路由键
//        rabbitTemplate.convertAndSend("exchangeName", "routingKey", chartTask);
//    }
}
