package com.yupi.moonBI.component;

import com.alibaba.fastjson.JSONObject;
import com.yupi.moonBI.model.sparkdto.MsgDTO;
import com.yupi.moonBI.model.sparkdto.ResponseDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 模型监听类：用于处理消息
 * 1. 继承自okhttp3.WebSocketListener，实现对WebSocket连接的监听和处理。
 * 提供了两个方法：`onMessage()` 和 `onClosed()`，分别用于处理接收到的消息和连接关闭事件。
 */
@Slf4j
public class SparkWebSocketListener extends WebSocketListener {
    //定义 `answer` 和 `wsCloseFlag` 两个私有变量，分别用于存储回答和关闭连接的标识。
    private StringBuilder answer = new StringBuilder();

    private boolean wsCloseFlag = false;

    //定义了 `getAnswer()` 和 `isWsCloseFlag()` 两个公共方法，分别用于获取回答和检查关闭连接的标识。
    public StringBuilder getAnswer() {
        return answer;
    }

    public boolean isWsCloseFlag() {
        return wsCloseFlag;
    }

    /**
     * 用于指定连接成功后的回调函数。
     * @param webSocket
     * @param response
     */
    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        super.onOpen(webSocket, response);
    }

    /**
     * 用于指定当从服务器接受到信息时的回调函数。
     * @param webSocket
     * @param text
     */
    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        super.onMessage(webSocket, text);
        /**
         * 接受消息处理思路：
         * 1、数据转化，接收JSON文本转化为java对象
         * 2、响应结果判断
         *   2.1、code不为0，响应错误，返回异常
         *   2.2、code为0，响应正确
         * 3、响应结果获取，并进行拼接
         * 4、获取计费token大小
         */
        // 1、数据转化，将大模型回复的 JSON 文本转为 ResponseDTO 对象
        ResponseDTO responseData = JSONObject.parseObject(text, ResponseDTO.class);
        /**
         * 2、根据 ResponseDTO 对象中的 header 字段中的 code 值判断是否为错误信息。
         */
        // 如果响应数据中的 header 的 code 值不为 0，则表示响应错误
        if (responseData.getHeader().getCode() != 0) {
            // 日志记录
            log.error("发生错误，错误码为：" + responseData.getHeader().getCode() + "; " + "信息：" + responseData.getHeader().getMessage());
            // 设置回答
            this.answer = new StringBuilder("大模型响应错误，请稍后再试");
            // 关闭连接标识
            wsCloseFlag = true;
            return;
        }
        /**
         * 3、响应结果获取，并进行拼接
         */
        // 响应数据中的header值为0，响应正常，获取回答
        ResponseDTO.PayloadDTO payload = responseData.getPayload();
        // 获取回答列表
        List<MsgDTO> choices = payload.getChoices().getText();
        // 将回答进行拼接
        for (MsgDTO msgDTO :  choices) {
            this.answer.append(msgDTO.getContent());
        }
        /**
         * 4、获取计费token大小
         */
        // 对最后一个文本结果进行处理，2表示最后一个结果
        if (2 == responseData.getHeader().getStatus()) {
            wsCloseFlag = true;
            ResponseDTO.PayloadDTO.UsageDTO.TextDTO testDto = payload.getUsage().getText();
            Integer totalTokens = testDto.getTotalTokens();
            System.out.println("本次花费："+totalTokens + " tokens");
            webSocket.close(3,"客户端主动断开链接");
        }
    }

    /**
     * 指定连接失败后的回调函数
     * @param webSocket
     * @param t
     * @param response
     */
    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        super.onFailure(webSocket, t, response);
    }

    /**
     * 用于指定连接关闭后的回调函数
     * @param webSocket
     * @param code
     * @param reason
     */
    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        super.onClosed(webSocket, code, reason);
    }
}
