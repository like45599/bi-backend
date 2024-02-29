package com.yupi.moonBI.strategy;

import cn.hutool.core.io.FileUtil;
import com.yupi.moonBI.bimqConfig.BIMessageProducer;
import com.yupi.moonBI.common.ErrorCode;
import com.yupi.moonBI.constant.ModelConstant;
import com.yupi.moonBI.exception.ThrowUtils;
import com.yupi.moonBI.manager.AIManager;
import com.yupi.moonBI.mapper.ChartMapper;
import com.yupi.moonBI.model.dto.chart.GenChartByAiRequest;
import com.yupi.moonBI.model.entity.Chart;
import com.yupi.moonBI.model.entity.User;
import com.yupi.moonBI.model.vo.BIResponse;
import com.yupi.moonBI.service.ChartService;
import com.yupi.moonBI.utils.ExcelUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
/**
 * 同步生成图表策略
 */
@Component("synchronousStrategy")
@Slf4j
public class SynchronousStrategy implements ChartGenerationStrategy {

    @Resource
    private ChartMapper chartMapper;

    @Resource
    private AIManager aiManager;

//    @Lazy
//    @Resource
//    private ChartService chartService;

    @Override
    public BIResponse generateChart(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser)  throws FileNotFoundException {
        // 实现同步处理逻辑
        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //文件校验
        vaildFile(multipartFile);

//        long biModelID = modelConstant.getModelId();

        /**
         * 将分析需求转成代码——
         *
         * ——分析需求：
         *         分析网站用户的增长情况
         *         原始数据：
         *         日期，用户数
         *         1号，10
         *         2号，20
         *         3号，30
         */
        //String promote = AIManager.PRECONDITION + "分析需求 " + goal + " \n原始数据如下: " + cvsData + "\n生成图标的类型是: " + chartType;
        //构造用户输入
        StringBuilder userInput = new StringBuilder();
        userInput.append(AIManager.PRECONDITION);
        userInput.append("分析需求：").append("\n");
        //拼接分析目标
        String userGoal = goal;
        if (StringUtils.isNotBlank(chartType)) {
            userGoal += "，请使用" + chartType;
        }
        userInput.append(userGoal).append("\n");
        userInput.append("原始数据:").append("\n");
        //压缩后的数据
        // 将excel文件转换为csv文件
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        // 将csv文件内容添加到userInput中
        userInput.append(csvData).append("\n");

        // 调用aiManager的doChart方法，传入biModelID和userInput
        String resultData = aiManager.sendMesToAISpark(userInput.toString());
//        String resultData = aiManager.sendMesToAISpark02(biModelID,promote);
        log.info("AI 生成的信息: {}", resultData);
        String[] splits = resultData.split("【【【【【");
        ThrowUtils.throwIf(splits.length < 3, ErrorCode.SYSTEM_ERROR, "AI 生成错误");

        // 获取生成的图表和结果
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        //插入到数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setName(name);
        chart.setChartType(chartType);
        chart.setUserId(loginUser.getId());
//        chart.setGenChart(genChart);
//        chart.setGenResult(genResult);
//        chart.setStatus("succeed");
        // 插入到MongoDB
        // 创建com.yupi.moonBI.model.document.Chart对象
        com.yupi.moonBI.model.document.Chart chartDocument = new com.yupi.moonBI.model.document.Chart();
        // 设置属性
        BeanUtils.copyProperties(chart, chartDocument);
        int saveResult = chartMapper.insert(chart);

        chartDocument.setChartId(chart.getId());
        chartDocument.setGenChart(genChart);
        chartDocument.setGenResult(genResult);

        // 将生成的数据保存到MongoDB
//        chartService.saveDocument(chartDocument);

        ThrowUtils.throwIf(saveResult != 1, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        Long chartId = chart.getId();

        Chart updateChart = new Chart();
        updateChart.setId(chartId);
        updateChart.setStatus("succeed");

        chartMapper.updateById(updateChart);

        // 创建BIResponse对象
        BIResponse biResponse = new BIResponse();
        // 设置生成的图表和结果
        biResponse.setGenChart(genChart);
        biResponse.setGenResult(genResult);
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
