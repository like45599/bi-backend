package com.yupi.moonBI.manager;

import io.github.briqt.spark4j.SparkClient;
import io.github.briqt.spark4j.constant.SparkApiVersion;
import io.github.briqt.spark4j.exception.SparkException;
import io.github.briqt.spark4j.model.SparkMessage;
import io.github.briqt.spark4j.model.SparkSyncChatResponse;
import io.github.briqt.spark4j.model.request.SparkRequest;
import io.github.briqt.spark4j.model.response.SparkTextUsage;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
class AIManagerTest {

    @Resource
    private AIManager aiManager;

    /**
     * 🐟聪明AI调用测试
     */
    @Test
    void doChart() {
        String chart = aiManager.doChart(1659171950288818178L, "分析需求：\n" +
                "分析网站用户的增长情况\n" +
                "原始数据：\n" +
                "日期，用户数\n" +
                "1号，10\n" +
                "2号，20\n" +
                "3号，30");
        System.out.println(chart);

    }

    /**
     * 星火模型调用测试
     */
    @Test
    void test01(){
        String messages="怎么学MySQL";
        String result = aiManager.sendMesToAISpark(messages);
        System.out.println(result);
    }


    /**
     * 星火模型调用（添加预设）
     */
    @Test
    void test() {
        SparkClient sparkClient = new SparkClient();
        // 设置认证信息
        String appid="e5461b1a";
        String apiKey="d627b4e65fd03f2a593982de2f50fc5d";
        String apiSecret="OTUyYjFjZTliMjk0NjQxY2FhMWM2YmVh";
        sparkClient.appid = appid;
        sparkClient.apiKey = apiKey;
        sparkClient.apiSecret = apiSecret;

        // 消息列表，可以在此列表添加历史对话记录
        List<SparkMessage> messages = new ArrayList<>();
        messages.add(SparkMessage.userContent("你是一个数据分析师和前端开发专家，接下来我会按照以下固定格式给你提供内容：\n" +
                "分析需求：\n" +
                "{数据分析的需求或者目标}\n" +
                "原始数据：\n" +
                "{csv格式的原始数据，用,作为分隔符}\n" +
                "请根据这两部分内容，严格按照以下指定格式生成内容（此外不要输出任何多余的开头、结尾、注释）\n" +
                "【【【【【\n" +
                "{前端 Echarts V5 的 option 配置对象 JSON 代码, 不要生成任何多余的内容，比如注释和代码块标记}\n" +
                "【【【【【 \n" +
                "{明确的数据分析结论、越详细越好，不要生成多余的注释} \n" +
                "最终格式是:  【【【【【 前端代码【【【【【  分析结论 \n" +
                "分析需求:分析一下用户趋势 原始数据如下: 用户id,用户数据,用户增量\n" +
                "1,200,10\n" +
                "2,200,20\n" +
                "3,800,10\n" +
                "4,90,30\n" +
                "5,800,10\n" +
                "9,800,20\n" +
                "7,800,10生成图标的类型是: 折线图"));


        // 构造请求
        SparkRequest sparkRequest = SparkRequest.builder()
                // 消息列表
                .messages(messages)
                // 模型回答的tokens的最大长度,非必传,取值为[1,4096],默认为2048
                .maxTokens(2048)
                // 核采样阈值。用于决定结果随机性,取值越高随机性越强即相同的问题得到的不同答案的可能性越高 非必传,取值为[0,1],默认为0.5
                .temperature(0.5)
                // 指定请求版本，默认使用最新2.0版本
                .apiVersion(SparkApiVersion.V3_5)
                .build();
        // 同步调用
        SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
        String content = chatResponse.getContent();
        System.out.println(content);
        String genChart0 = content.split("【【【【【")[0].trim();
        String genChart = content.split("【【【【【")[1].trim();
        String genResult = content.split("【【【【【")[2].trim();
        System.out.println("genChart0 = " + genChart0);
        System.out.println("genResult = " + genResult);
        System.out.println("genChart = " + genChart);

    }

    @Test
    void test1() {
        SparkClient sparkClient = new SparkClient();
        // 设置认证信息
        String appid="e5461b1a";
        String apiKey="d627b4e65fd03f2a593982de2f50fc5d";
        String apiSecret="OTUyYjFjZTliMjk0NjQxY2FhMWM2YmVh";
        sparkClient.appid = appid;
        sparkClient.apiKey = apiKey;
        sparkClient.apiSecret = apiSecret;

        // 消息列表，可以在此列表添加历史对话记录
        List<SparkMessage> messages=new ArrayList<>();
        messages.add(SparkMessage.systemContent("请你扮演我的语文老师李老师，问我讲解问题问题，希望你可以保证知识准确，逻辑严谨。"));
        messages.add(SparkMessage.userContent("鲁迅和周树人小时候打过架吗？"));
// 构造请求
        SparkRequest sparkRequest=SparkRequest.builder()
// 消息列表
                .messages(messages)
                .maxTokens(2048)
                .temperature(0.2)
                .apiVersion(SparkApiVersion.V3_5)
                .build();

        try {
            // 同步调用
            SparkSyncChatResponse chatResponse = sparkClient.chatSync(sparkRequest);
            SparkTextUsage textUsage = chatResponse.getTextUsage();

            System.out.println("\n回答：" + chatResponse.getContent());
            System.out.println("\n提问tokens：" + textUsage.getPromptTokens()
                    + "，回答tokens：" + textUsage.getCompletionTokens()
                    + "，总消耗tokens：" + textUsage.getTotalTokens());
        } catch (SparkException e) {
            System.out.println("发生异常了：" + e.getMessage());
        }

    }


}