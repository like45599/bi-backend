package com.yupi.moonBI.config;

import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 线程池配置类
 */
@Configuration
public class ThreadPoolExecutorConfig {

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        // 创建线程工厂
        ThreadFactory threadFactory = new ThreadFactory() {
            private int count = 1;

            @Override
            public Thread newThread(@NotNull Runnable r) {
                // 创建一个新的线程
                Thread thread = new Thread(r);
                // 为线程设置一个名称
                thread.setName("线程-" + count++);
                // 返回新创建的线程
                return thread;
            }
        };
        /**
         * 创建线程池：
         * 参数1：核心线程数（正式工）——2个
         * 参数2：最大线程数（工位总数）——4个
         * 参数3：线程存活时间——100秒
         * 参数4：线程队列容量（备忘录容量）——4
         * 参数5：线程工厂
         *
         */
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 4, 100,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(4), threadFactory);
        return threadPoolExecutor;
    }
}
