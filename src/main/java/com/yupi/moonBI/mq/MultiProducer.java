package com.yupi.moonBI.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.util.Scanner;


public class MultiProducer {
    private static final String TASK_QUEUE_NAME = "mult_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.88.130");
        factory.setPort(5672);
        //connect：创建一个Connection对象：使用ConnectionFactory创建一个Connection对象，用于与RabbitMQ消息代理建立连接。
        //channel：创建一个Channel对象：使用Connection对象创建一个Channel对象，用于向队列发送消息。
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            // 声明一个名为TASK_QUEUE_NAME的队列，并且设置持久化消息
            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNext()) {
                String message = scanner.nextLine();
                // 发布消息，消息持久化类型为PERSISTENT_TEXT_PLAIN，消息内容为message的UTF-8编码
                channel.basicPublish("", TASK_QUEUE_NAME,
                        MessageProperties.PERSISTENT_TEXT_PLAIN,
                        message.getBytes("UTF-8"));
                System.out.println(" [x] Sent '" + message + "'");
            }
        }
    }
}
