package com.yupi.moonBI.bimq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 用于测试程序用到的交换机和队列（只在程序启动前执行一次）
 */
public class MqInitMain {
    public static void main(String[] args) {
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("localhost");
            factory.setPort(5672);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();

            String EXCHANGE_NAME = "code_exchange";
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");

            // 声明一个队列，并且设置持久化消息
            String queueName = "code_queue";
            channel.queueDeclare(queueName, true, false, false, null);
            //队列绑定交换机，routing_key用于指定消息应该发送到哪个队列。
            channel.queueBind(queueName, EXCHANGE_NAME, "my_RoutingKey");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
