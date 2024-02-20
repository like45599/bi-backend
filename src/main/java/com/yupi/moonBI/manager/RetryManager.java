package com.yupi.moonBI.manager;

import com.github.rholder.retry.*;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * AI响应重试管理器
 */
@Component
public class RetryManager implements RetryListener {

    int maxRetryCount = 4;//最大重试次数

    // 定义重试器
    public Retryer<String> retryer() {
        Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
                //设置异常重试源
                .retryIfExceptionOfType(RuntimeException.class)
                .retryIfException()//只要出现异常都重试
                .retryIfResult(res->res.equals("AI生成错误")) // 如果结果为false则重试
                .withWaitStrategy(WaitStrategies.fixedWait(3, TimeUnit.SECONDS)) // 等待3秒
                .withStopStrategy(StopStrategies.stopAfterAttempt(maxRetryCount)) // 允许执行4次（首次执行 + 最多重试3次）
                .withRetryListener(this) // 注册RetryListener
                .build();
        return retryer;
    }


    /**
     * 重试监听器
     * @param attempt
     * @param <V>
     */
    @Override
    public <V> void onRetry(Attempt<V> attempt) {
        // 判断当前重试是否已经出现异常
        if (attempt.hasException()) {
            System.out.println("重试出现异常：" + attempt.getExceptionCause().getMessage());
        }
        System.out.println("AI接口第" + attempt.getAttemptNumber() + "次重试");

        // 判断当前重试次数是否达到最大重试次数
        if (attempt.getAttemptNumber() == maxRetryCount) {
            System.out.println("接口重试次数已耗尽！！！");
        }
    }
}
