package com.yupi.moonBI.bimqConfig;

import com.yupi.moonBI.MQConstant.BiMqConstant;
import com.rabbitmq.client.Channel;
import com.yupi.moonBI.common.ErrorCode;
import com.yupi.moonBI.constant.ModelConstant;
import com.yupi.moonBI.exception.BusinessException;
import com.yupi.moonBI.manager.AIManager;
import com.yupi.moonBI.model.entity.Chart;
import com.yupi.moonBI.service.ChartService;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

@Component
@Slf4j
public class BIMessageConsumer {
    @Resource
    private ChartService chartService;

    @Resource
    private AIManager aiManager;

    @Resource
    private ModelConstant modelConstant;

    @Autowired
    private RabbitTemplate rabbitTemplate;


//    @Resource
//    WebSocketServer webSocketServer;
    /**
     * 接收消息的方法
     *
     * @param
     * @param channel
     * @param deliveryTag
     */
    //使用@SneakyThrows注解简化异常处理
    @SneakyThrows
    @RabbitListener(queues = {BiMqConstant.BI_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receiveMessage message = {}", message);
        try {
            if (StringUtils.isBlank(message)) {
                // 如果失败，消息拒绝
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "消息为空");
            }
            long chartId = Long.parseLong(message);
            Chart chart = chartService.getById(chartId);
            if (chart == null) {
                channel.basicNack(deliveryTag, false, false);
                throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "图表为空");
            }
            // 先修改图表任务状态为 “执行中”。等执行成功后，修改为 “已完成”、保存执行结果；执行失败后，状态修改为 “失败”，记录任务失败信息。
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            boolean b = chartService.updateById(updateChart);
            if (!b) {
                channel.basicNack(deliveryTag, false, false);
                handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
                return;
            }
            // 调用 AI
            String result = aiManager.sendMesToAISpark( AIManager.PRECONDITION + buildUserInput(chart));
            String[] splits = result.split("【【【【【");
            if (splits.length < 3) {
                channel.basicNack(deliveryTag, false, false);
                handleChartUpdateError(chart.getId(), "AI 生成错误");
                return;
            }
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            // todo 建议定义状态为枚举值
            updateChartResult.setStatus("succeed");
            boolean updateResult = chartService.updateById(updateChartResult);
            if (!updateResult) {
                channel.basicNack(deliveryTag, false, false);
                handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
            }
            // 消息确认
//            webSocketServer.sendMessage("您的[" + chart.getName() + "]生成成功 , 前往 我的图表 进行查看", new HashSet<>(Arrays.asList(chart.getUserId().toString())));

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理消息失败, 将消息发送到死信队列: " + e.getMessage());

            // 消息处理失败，拒绝消息并不重新排队，这样它将被发送到死信队列
            channel.basicNack(deliveryTag, false, false);
        }
    }


    /**
     * 构建用户输入
     *
     * @param chart
     * @return
     */
    public String buildUserInput(Chart chart) {
        String goal = chart.getGoal();
        String chartType = chart.getChartType();
        String csvData = chart.getChartData();
        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append("分析需求：").append("\n");

        //拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:").append("\n");
        // 将csv文件内容添加到userInput中
        userInput.append(csvData).append("\n");
        return userInput.toString();
    }

    /**
     * 上面接口很多用到异常
     * 创建一个回调函数，对图表状态失败这一情况进行集中异常处理
     */
    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setExecMessage(execMessage);
        updateChartResult.setStatus("failed");
        boolean updateResult = chartService.updateById(updateChartResult);
        if (!updateResult) {

            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }
}
