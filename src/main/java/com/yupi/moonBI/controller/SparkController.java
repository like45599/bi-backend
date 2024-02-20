package com.yupi.moonBI.controller;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.StrUtil;
import com.github.rholder.retry.RetryException;
import com.yupi.moonBI.component.SparkClient02;
import com.yupi.moonBI.component.SparkWebSocketListener;
import com.yupi.moonBI.config.SparkConfig;
import com.yupi.moonBI.manager.AIManager;
import com.yupi.moonBI.manager.RetryManager;
import com.yupi.moonBI.model.sparkdto.MsgDTO;
import com.yupi.moonBI.service.SparkService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
@RestController
@RequestMapping("/spark")
@Slf4j
public class SparkController {

    @Resource
    private SparkClient02 sparkClient02;

    @Resource
    private SparkConfig sparkConfig;

    @Resource
    private AIManager aiManager;

    @Resource
    private SparkService sparkService;

    @Resource
    private RetryManager retryManager;

    /**
     * 发送问题
     *
     * @param question 问题
     * @return 星火大模型的回答
     */
    @GetMapping("/sendQuestion")
    public String sendQuestion(@RequestParam("question") String question) {
        // 如果是无效字符串，则不对大模型进行请求
        if (StrUtil.isBlank(question)) {
            return "无效问题，请重新输入";
        }
        // 获取连接令牌
        if (!sparkClient02.operateToken(SparkClient02.GET_TOKEN_STATUS)) {
            return "当前大模型连接数过多，请稍后再试";
        }

        // 创建消息对象
        MsgDTO msgDTO = MsgDTO.createUserMsg(question);
        // 创建监听器
        SparkWebSocketListener listener = new SparkWebSocketListener();
        // 发送问题给大模型，生成 websocket 连接
        WebSocket webSocket = sparkClient02.sendMsg(UUID.randomUUID().toString().substring(0, 10), Collections.singletonList(msgDTO), listener);
        if (webSocket == null) {
            // 归还令牌
            sparkClient02.operateToken(SparkClient02.BACK_TOKEN_STATUS);
            return "系统内部错误，请联系管理员";
        }
        try {
            int count = 0;
            // 为了避免死循环，设置循环次数来定义超时时长
            int maxCount = sparkConfig.getMaxResponseTime() * 5;
            while (count <= maxCount) {
                Thread.sleep(200);
                if (listener.isWsCloseFlag()) {
                    break;
                }
                count++;
            }
            if (count > maxCount) {
                return "大模型响应超时，请联系管理员";
            }
            // 响应大模型的答案
            return listener.getAnswer().toString();
        } catch (InterruptedException e) {
            log.error("错误：" + e.getMessage());
            return "系统内部错误，请联系管理员";
        } finally {
            // 关闭 websocket 连接
            webSocket.close(1000, "");
            // 归还令牌
            sparkClient02.operateToken(SparkClient02.BACK_TOKEN_STATUS);
        }
    }

    /**
     * 星火大模型优化
     * 发送问题
     *
     * @param question 问题
     * @return 星火大模型的回答
     */
    @GetMapping("/sendQuestion02")
    public String sendQuestion02(@RequestParam("question") String question) {
        // 如果是无效字符串，则不对大模型进行请求
        if (StrUtil.isBlank(question)) {
            return "无效问题，请重新输入";
        }
        String answer = sparkService.sparkAIsendQuestion(question);
        return answer;
    }

    /**
     * RetryableExecutor 重试方法
     * @param param
     * @return
     * @throws ExecutionException
     * @throws RetryException
     */
    @GetMapping("/SendFailRetry")
    public String SendFailRetry(String param) throws ExecutionException, RetryException {

        Callable<String> callable = () -> {
            // 发起重试请求的逻辑
            String response = sendQuestion02(param);
            if (response != null) {
                return response;
            } else {
                throw new RuntimeException("AI响应异常，请重试");
            }
        };
        String callBack = retryManager.retryer().call(callable);
        return callBack;
    }


}
