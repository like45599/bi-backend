package com.yupi.moonBI;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


/**
 * 主类（项目启动入口）
 *
 */
// 如需开启 Redis，须移除 exclude 中的内容
//@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@SpringBootApplication
@MapperScan("com.yupi.moonBI.mapper")
@EnableScheduling
@EnableWebMvc
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
//@EnableMongoRepositories(basePackages = "com.yupi.moonBI.repository")
public class MainApplication {

    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }

}
