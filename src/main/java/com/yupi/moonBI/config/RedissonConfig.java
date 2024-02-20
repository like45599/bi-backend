package com.yupi.moonBI.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.redisson")
@Data
public class RedissonConfig {
    private String host;
    private Integer database;
    private Integer port;
    private String password;

    //spring启动时，自动创建RedissonClient对象
    @Bean
    public RedissonClient getRedissonClient() {
        Config config = new Config();
        // 设置使用单个服务器
        config.useSingleServer()
                // 设置数据库
                .setDatabase(database)
                // 设置地址
                .setAddress("redis://" + host + ":" + port)
                // 设置密码
                .setPassword(password);
        // 创建Redisson客户端
        RedissonClient redisson = Redisson.create(config);
        return redisson;
    }
}
