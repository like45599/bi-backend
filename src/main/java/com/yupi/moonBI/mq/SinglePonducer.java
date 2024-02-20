package com.yupi.moonBI.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class SinglePonducer {

    public static final String QUEUE_NAME = "hello";

    public static void main(String[] args) throws IOException, TimeoutException {
        //1、创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.88.130");//ip 默认值localhost
        factory.setPort(5672);//设置端口
        //3、创建连接Connection
        Connection connection = factory.newConnection();
        //4、创建Channel
        Channel channel = connection.createChannel();
        //5、创建队列Queue
        //如果没有一个名字叫hello_world的队列，则会创建该队列，如果有则不会创建
        channel.queueDeclare(QUEUE_NAME, true, false, false, null);

        String message = "hello rabbitmq~~~";
        //6、发送消息
        //如果使用的是默认的交换机，那路由名称必须要和队列名称相同
        channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
        System.out.println(message);

        //7、释放资源
        channel.close();
        connection.close();
    }
}
