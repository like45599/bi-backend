package com.yupi.moonBI.component;

import com.alibaba.fastjson.JSONObject;
import com.yupi.moonBI.config.SparkConfig;
import com.yupi.moonBI.model.sparkdto.MsgDTO;
import com.yupi.moonBI.model.sparkdto.RequestDTO;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 模型客户端类：实现操作令牌、发送请求、接收响应、鉴权等功能
 */
@Component
@Slf4j
public class SparkClient02 {

    /**
     * 获取令牌
     */
    public static int GET_TOKEN_STATUS = 0;
    /**
     * 归还令牌
     */
    public static int BACK_TOKEN_STATUS = 1;
    @Autowired
    private SparkConfig sparkConfig;
    @Value("${xfxh.QPS}")
    private int connectionTokenCount;

    /**
     * 操作令牌
     *
     * @param status 0-获取令牌 1-归还令牌
     * @return 是否操作成功
     */
    public synchronized boolean operateToken(int status) {
        if (status == GET_TOKEN_STATUS) {
            // 获取令牌
            if (connectionTokenCount != 0) {
                // 说明还有令牌，将令牌数减一
                connectionTokenCount -= 1;
                log.debug("令牌有余，获取令牌成功");
                return true;
            } else {
                log.debug("令牌不足，获取令牌失败");
                return false;
            }
        } else {
            // 放回令牌
            connectionTokenCount += 1;
            log.debug("操作结束了，归还令牌成功！");
            return true;
        }
    }

    /**
     * 发送消息
     *
     * @param uid     每个用户的id，用于区分不同用户
     * @param msgList 发送给大模型的消息，可以包含上下文内容
     * @return 获取websocket连接，以便于我们在获取完整大模型回复后手动关闭连接
     */
    public WebSocket sendMsg(String uid, List<MsgDTO> msgList, WebSocketListener listener) {
        // 获取鉴权url
        String authUrl = this.getAuthUrl();
        // 鉴权方法生成失败，直接返回 null
        if (authUrl == null) {
            return null;
        }
        OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
        // 将 https/http 连接替换为 ws/wss 连接
        String url = authUrl.replace("http://", "ws://").replace("https://", "wss://");
        Request request = new Request.Builder().url(url).build();
        // 建立 wss 连接
        WebSocket webSocket = okHttpClient.newWebSocket(request, listener);
        // 组装请求参数
        RequestDTO requestDTO = getRequestParam(uid, msgList);
        // 发送请求
        webSocket.send(JSONObject.toJSONString(requestDTO));
        return webSocket;
    }

    /**
     * 生成鉴权方法，具体实现不用关心，这是讯飞官方定义的鉴权方式
     *
     * @return 鉴权访问大模型的路径
     */
    public String getAuthUrl() {
        try {
            URL url = new URL(sparkConfig.getHostUrl());
            // 时间
            SimpleDateFormat format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String date = format.format(new Date());
            // 拼接
            String preStr = "host: " + url.getHost() + "\n" +
                    "date: " + date + "\n" +
                    "GET " + url.getPath() + " HTTP/1.1";
            // SHA256加密
            Mac mac = Mac.getInstance("hmacsha256");
            SecretKeySpec spec = new SecretKeySpec(sparkConfig.getApiSecret().getBytes(StandardCharsets.UTF_8), "hmacsha256");
            mac.init(spec);

            byte[] hexDigits = mac.doFinal(preStr.getBytes(StandardCharsets.UTF_8));
            // Base64加密
            String sha = Base64.getEncoder().encodeToString(hexDigits);
            // 拼接
            String authorizationOrigin = String.format("api_key=\"%s\", algorithm=\"%s\", headers=\"%s\", signature=\"%s\"", sparkConfig.getApiKey(), "hmac-sha256", "host date request-line", sha);
            // 拼接地址
            HttpUrl httpUrl = Objects.requireNonNull(HttpUrl.parse("https://" + url.getHost() + url.getPath())).newBuilder().
                    addQueryParameter("authorization", Base64.getEncoder().encodeToString(authorizationOrigin.getBytes(StandardCharsets.UTF_8))).
                    addQueryParameter("date", date).
                    addQueryParameter("host", url.getHost()).
                    build();

            return httpUrl.toString();
        } catch (Exception e) {
            log.error("鉴权方法中发生错误：" + e.getMessage());
            return null;
        }
    }

    /**
     * 获取请求参数
     *
     * @param uid     每个用户的id，用于区分不同用户
     * @param msgList 发送给大模型的消息，可以包含上下文内容
     * @return 请求DTO，该 DTO 转 json 字符串后生成的格式参考 resources/demo-json/request.json
     */
    public RequestDTO getRequestParam(String uid, List<MsgDTO> msgList) {
        RequestDTO requestDTO = new RequestDTO();
        //获取应用ID
        String appId = sparkConfig.getAppId();
        // 使用应用ID和用户ID创建一个HeaderDTO对象，并将其设置为请求的头部
        RequestDTO.HeaderDTO headerDTO = new RequestDTO.HeaderDTO(appId, uid);
        requestDTO.setHeader(headerDTO);

        // 从sparkConfig对象中获取域名、采样阈值和模型回答token的最大长度
        String domain = sparkConfig.getDomain();
        Double temperature = sparkConfig.getTemperature();
        Integer maxTokens = sparkConfig.getMaxTokens();

        // 使用这些值创建一个ChatDTO对象
        RequestDTO.ParameterDTO.ChatDTO chatDTO = new RequestDTO.ParameterDTO.ChatDTO(domain, temperature, maxTokens);
        // 使用ChatDTO对象创建一个ParameterDTO对象，并将其设置为请求的参数
        RequestDTO.ParameterDTO parameterDTO = new RequestDTO.ParameterDTO(chatDTO);
        requestDTO.setParameter(parameterDTO);

        // 使用msgList创建一个MessageDTO对象，并将其设置为请求的有效载荷
        RequestDTO.PayloadDTO.MessageDTO messageDTO = new RequestDTO.PayloadDTO.MessageDTO(msgList);
        RequestDTO.PayloadDTO payloadDTO = new RequestDTO.PayloadDTO(messageDTO);
        requestDTO.setPayload(payloadDTO);
        return requestDTO;
    }

}
