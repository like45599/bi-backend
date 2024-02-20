package com.yupi.moonBI.manager;

import com.yupi.moonBI.common.ErrorCode;
import com.yupi.moonBI.config.SparkConfig;
import com.yupi.moonBI.exception.BusinessException;
import com.yupi.yucongming.dev.client.YuCongMingClient;
import com.yupi.yucongming.dev.common.BaseResponse;
import com.yupi.yucongming.dev.model.DevChatRequest;
import com.yupi.yucongming.dev.model.DevChatResponse;
import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于对接AI平台
 */
@Service
public class AIManager {
    @Resource
    private YuCongMingClient yuCongMingClient;

    @Resource
    private SparkClient sparkClient;

    @Autowired
    private SparkConfig sparkConfig;

    /**
     * AI 生成问题的预设条件
     */
    public static final String PRECONDITION = "你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
            "分析需求：\n" +
            "{数据分析的需求或者目标}\n" +
            "原始数据：\n" +
            "{csv格式的原始数据，用,作为分隔符}\n" +
            "请根据这两部分内容，必须严格按照以下指定格式生成内容。此外不要输出任何多余的开头、结尾、注释、代码块标记\n" +

            "【【【【【\n" +
            "{前端代码(Echarts V5 的 option 配置对象 JSON )。 不要生成任何多余的内容，比如注释和代码块标记}\n" +
            "【【【【【\n" +
            "{明确的数据分析结论、越详细越好，不要生成多余的注释} \n" +

            "最终格式是:  【【【【【  前端JSON代码 【【【【【 分析结论 \n" +

            "注意：`前端JSON代码`是指`option = ?;`中 ? 的内容; 只有两个`【【【【【`他们的作用是区别代码和结论以便后端通过split进行解析，不需要输出`前端JSON代码`等字样;两个'【【【【【'是划分内容的标志是固定的也不要生成额外的【】";


    /**
     * 🐟聪明Ai对话
     *
     * @param message
     * @return
     */
    // 创建DevChatRequest对象
    public String doChart(long modelId, String message) {
        DevChatRequest devChatRequest = new DevChatRequest();
        // 设置模型ID
        //devChatRequest.setModelId(1651468516836098050L);
        devChatRequest.setModelId(modelId);
        // 设置消息
        devChatRequest.setMessage(message);
        // 调用AI接口
        BaseResponse<DevChatResponse> response = yuCongMingClient.doChat(devChatRequest);
        // 判断响应是否为空
        if (response == null) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI响应异常");
        }
        // 返回AI接口响应的内容
        return response.getData().getContent();
    }

    /**
     * 星火 AI 问答
     * @return
     */
    public String sendMesToAISpark(final String content) {
        // 创建消息列表
        List<SparkMessage> messages = new ArrayList<>();
        // 添加消息
        messages.add(SparkMessage.userContent(content));
        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
                // 消息列表
                .messages(messages)
                // 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
                .maxTokens(sparkConfig.getMaxTokens())
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(sparkConfig.getTemperature())
                // 指定请求版本，默认使用最新2.0版本
                .apiVersion(SparkApiVersion.V3_5)
                .build();
        // 同步调用
        SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
        // 返回消息内容
        return chatResponse.getContent();
    }

    /**
     * 星火AI对话（智能分析）
     * @param content
     * @return
     */
    public String sendMesToAISpark02(long modelId,  String content) {
        // 创建消息列表
        List<SparkMessage> messages = new ArrayList<>();
        // 添加消息
        messages.add(SparkMessage.userContent(content));
        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
                // 消息列表
                .messages(messages)
                // 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
                .maxTokens(sparkConfig.getMaxTokens())
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(sparkConfig.getTemperature())
                // 指定请求版本，默认使用最新2.0版本
                .apiVersion(SparkApiVersion.V2_0)
                .build();
        // 同步调用
        SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
        // 返回消息内容
        return chatResponse.getContent();
    }
}
