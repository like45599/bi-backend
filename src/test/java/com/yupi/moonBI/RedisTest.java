package com.yupi.moonBI;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

public class RedisTest {
    public static void main(String[] args) {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://123.57.241.179:6379")
                .setPassword("123321");

        RedissonClient redisson = Redisson.create(config);
        try {
            redisson.getKeys().getKeys();
            System.out.println("Connected to Redis");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to connect to Redis");
        } finally {
            redisson.shutdown();
        }
    }
}
