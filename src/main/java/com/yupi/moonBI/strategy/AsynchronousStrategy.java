package com.yupi.moonBI.strategy;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yupi.moonBI.bimqConfig.BIMessageProducer;
import com.yupi.moonBI.common.ErrorCode;
import com.yupi.moonBI.constant.ModelConstant;
import com.yupi.moonBI.exception.BusinessException;
import com.yupi.moonBI.exception.ThrowUtils;
import com.yupi.moonBI.manager.AIManager;
import com.yupi.moonBI.mapper.ChartMapper;
import com.yupi.moonBI.model.dto.chart.ChartTask;
import com.yupi.moonBI.model.dto.chart.GenChartByAiRequest;
import com.yupi.moonBI.model.entity.Chart;
import com.yupi.moonBI.model.entity.User;
import com.yupi.moonBI.model.vo.BIResponse;
import com.yupi.moonBI.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
/**
 * 异步生成图表策略
 */
@Component("AsynchronousStrategy")
@Slf4j
public class AsynchronousStrategy implements ChartGenerationStrategy {

    //注入
    @Resource
    private ModelConstant modelConstant;
    @Resource
    private BIMessageProducer biMessageProducer;
    @Resource
    private ChartMapper chartMapper;

    @Resource
    private AIManager aiManager;

    @Override
    public BIResponse generateChart(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) throws IOException {
        // 实现异步处理逻辑，例如将请求推送到消息队列
        // 文件校验
        vaildFile(multipartFile);

        // 将excel文件转换为csv文件
        String csvData = ExcelUtils.excelToCsv(multipartFile);

        // 创建并插入Chart对象
        Chart chart = new Chart();
        chart.setGoal(genChartByAiRequest.getGoal());
        chart.setChartData(csvData);
        chart.setName(genChartByAiRequest.getName());
        chart.setChartType(genChartByAiRequest.getChartType());
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        int saveResult = chartMapper.insert(chart);
        if (saveResult <= 0) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图表保存失败");
        }

        // 创建ChartTask对象
        ChartTask chartTask = new ChartTask();
        chartTask.setId(chart.getId());  // 将Chart的id设置到ChartTask中
        chartTask.setMultipartFile(multipartFile);
        chartTask.setGenChartByAiRequest(genChartByAiRequest);
        chartTask.setUser(loginUser);

        // 创建 ObjectMapper 对象
        ObjectMapper objectMapper = new ObjectMapper();
        // 将 ChartTask 对象转换为 JSON 字符串
        String chartTaskJson = objectMapper.writeValueAsString(chartTask);

        // 将 JSON 字符串发送到消息队列
        biMessageProducer.sendMessage(chartTaskJson);

        // 创建BIResponse对象
        BIResponse biResponse = new BIResponse();
        biResponse.setChartId(chart.getId());
        return biResponse;


    }

    /**
     * 校验文件
     *
     * @param multipartFile
     */
    private void vaildFile(MultipartFile multipartFile) {
        // 获取文件大小
        long size = multipartFile.getSize();
        String originalFilename = multipartFile.getOriginalFilename();

        //校验文件大小
        final long ONE_MB = 1024 * 1024L;
        ThrowUtils.throwIf(size > ONE_MB, ErrorCode.PARAMS_ERROR, "文件过大,超过1M");
        //校验文件后缀
        String suffix = FileUtil.getSuffix(originalFilename);
        final List<String> vaildFileSuffixList = Arrays.asList("png", "jpg", "jpeg", "svg", "webp", "xlsx");
        ThrowUtils.throwIf(!vaildFileSuffixList.contains(suffix), ErrorCode.PARAMS_ERROR, "文件后缀非法");
    }

    /**
     * 上面接口很多用到异常
     * 创建一个回调函数，对图表状态失败这一情况进行集中异常处理
     */
    private void handleChartUpdateError(long chartId, String execMessage) {
        Chart updateChartResult = new Chart();
        updateChartResult.setId(chartId);
        updateChartResult.setExecMessage("execMessage");
        updateChartResult.setStatus("failed");
        int updateResult = chartMapper.updateById(updateChartResult);
        if (updateResult <= 0) {
            log.error("更新图表失败状态失败" + chartId + "," + execMessage);
        }
    }


}
