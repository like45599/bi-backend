package com.yupi.moonBI.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池测试
 */
@RestController
@RequestMapping("/queue")
@Slf4j
//只对开发环境和本地生效，正式上线需要把这个测试去掉
@Profile({"dev", "local"})
public class QueueController {
    //引入我们新定义的线程池
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    //新增任务的接口
    @GetMapping("/add")
    public void add(String name) {
        /**
         * 提交任务到线程池
         */
        //使用CompletableFuture运行一个异步任务
        CompletableFuture.runAsync(() -> {
            log.info("任务执行中" + name + "执行人：" + Thread.currentThread().getName());
            try {
                //睡60分钟,模拟长时间运行任务
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }, threadPoolExecutor);
    }

    //获取线程池信息
    @GetMapping("/get")
    public String get() {
        Map<String, Object> map = new HashMap<>();
        int size = threadPoolExecutor.getQueue().size();
        map.put("任务总数", threadPoolExecutor.getTaskCount());
        map.put("正在工作的线程数", threadPoolExecutor.getActiveCount());
        map.put("已完成任务数", threadPoolExecutor.getCompletedTaskCount());
        map.put("队列长度", size);
        return JSONUtil.toJsonStr(map);
    }
}
