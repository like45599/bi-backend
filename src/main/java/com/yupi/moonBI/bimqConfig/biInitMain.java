package com.yupi.moonBI.bimqConfig;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.yupi.moonBI.MQConstant.BiMqConstant;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static com.yupi.moonBI.MQConstant.BiMqConstant.*;

/**
 * 用于测试程序用到的交换机和队列（只在程序启动前执行一次）
 */
public class biInitMain {


    public static void main(String[] args) {
        // ConnectionFactory配置
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("123.57.241.179"); // 或者是您的RabbitMQ服务器地址
        // 如果需要，还可以设置端口、用户名和密码
         factory.setPort(5672);
         factory.setUsername("gaolike");
         factory.setPassword("123456");

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {

            // 声明死信交换机
            String deadLetterExchange = "deadLetterExchange";
            channel.exchangeDeclare(deadLetterExchange, "direct", true);

            // 声明死信队列
            String deadLetterQueueName = "deadLetterQueue";
            channel.queueDeclare(deadLetterQueueName, true, false, false, null);
            // 死信队列绑定到死信交换机
            String deadLetterRoutingKey = "deadLetterRoutingKey";
            channel.queueBind(deadLetterQueueName, deadLetterExchange, deadLetterRoutingKey);

            // 声明主工作队列，并设置死信队列参数
            String workQueueName = "bi_queue";
            String workRoutingKey = "bi_routingKey"; // 这里根据实际情况设置
            String exchangeName = "bi_exchange";
            channel.exchangeDeclare(exchangeName, "direct", true);
            // 设置死信交换机参数
            Map<String, Object> mqArgs = new HashMap<>();
            mqArgs.put("x-dead-letter-exchange", deadLetterExchange);
            mqArgs.put("x-dead-letter-routing-key", deadLetterRoutingKey);
            channel.queueDeclare(workQueueName, true, false, false, mqArgs);
            // 主工作队列绑定到主交换机
            channel.queueBind(workQueueName, exchangeName, workRoutingKey);

            System.out.println("RabbitMQ setup completed successfully.");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to setup RabbitMQ: " + e.getMessage());
        }
    }
}