package com.yupi.moonBI.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class MultiConsumer {
    private static final String TASK_QUEUE_NAME = "mult_queue";

    public static void main(String[] argv) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.88.130");
        factory.setPort(5672);
        final Connection connection = factory.newConnection();

        for (int i = 0; i < 2; i++) {
            //创建一个Channel对象，用于与RabbitMQ服务器进行通信
            final Channel channel = connection.createChannel();

            channel.queueDeclare(TASK_QUEUE_NAME, true, false, false, null);
            System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

            // 设置消息质量，一次只发送一条消息
            channel.basicQos(1);

            int finall = i;
            // 定义回调函数，接收消息，如何处理消息？
            DeliverCallback deliverCallback = (consumerTag, delivery) -> {
                String message = new String(delivery.getBody(), "UTF-8");

                try {
                    //处理工作
                    System.out.println(" [x] Received '" + "编号：" + finall + ":" + message + "'");
                    //睡个20秒，模拟机器处理消息能力有限
                    Thread.sleep(20000);
                    //手动确认消息签收，消费方确认收到快递
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                } catch (InterruptedException e) {
                    channel.basicNack(delivery.getEnvelope().getDeliveryTag(), false, false);
                    e.printStackTrace();
                } finally {
                    System.out.println(" [x] Done");
                    // 手动确认消息，如果自动确认，则不需要手动确认
                    channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                }
            };
            // 消费消息，消费者确认消息，开启消费监听
            channel.basicConsume(TASK_QUEUE_NAME, false, deliverCallback, consumerTag -> {
            });
        }
    }
}
