package com.yupi.moonBI.manager;

import com.github.rholder.retry.RetryException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.remoting.RemoteAccessException;

import javax.annotation.Resource;
import java.util.concurrent.ExecutionException;
@SpringBootTest
@Slf4j
class RetryManagerTest {

    @Resource
    private RetryManager retryManager;

    /**
     * 重试方法
     * @return
     */
    public static String retryTask()  {
        int i = RandomUtils.nextInt(0,11);
        log.info("随机生成的数:{}",i);
        if (i == 0) {
            log.info("为0,抛出参数异常.");
            throw new IllegalArgumentException("参数异常");
        }else if (i  == 1){
            log.info("为1,返回true.");
            return "返回true";
        }else if (i == 2){
            log.info("为2,返回false.");
            return "返回false";
        }else{
            //为其他
            throw new RemoteAccessException("大于2,抛出远程访问异常");
        }
    }

    @Test
    void retryer() throws ExecutionException, RetryException {
        retryManager.retryer().call(()-> retryTask());
    }
}