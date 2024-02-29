package com.yupi.moonBI.service.impl;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yupi.moonBI.bimqConfig.BIMessageProducer;
import com.yupi.moonBI.common.ErrorCode;
import com.yupi.moonBI.constant.ModelConstant;
import com.yupi.moonBI.exception.BusinessException;
import com.yupi.moonBI.exception.ThrowUtils;
import com.yupi.moonBI.manager.AIManager;
import com.yupi.moonBI.manager.RedisLimiterManager;
import com.yupi.moonBI.mapper.ChartMapper;
import com.yupi.moonBI.model.dto.chart.ChartTask;
import com.yupi.moonBI.model.dto.chart.GenChartByAiRequest;
import com.yupi.moonBI.model.entity.Chart;
import com.yupi.moonBI.model.entity.User;
import com.yupi.moonBI.model.vo.BIResponse;
import com.yupi.moonBI.service.ChartService;
import com.yupi.moonBI.strategy.ChartGenerationStrategy;
import com.yupi.moonBI.utils.ExcelUtils;
import com.yupi.moonBI.utils.ServerMetricsUtil;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.yupi.moonBI.common.ErrorCode.NOT_FOUND_ERROR;
import static com.yupi.moonBI.constant.RedisConstant.*;

/**
 * @author chenliang
 * @description 针对表【chart(图表信息表)】的数据库操作Service实现
 * @createDate 2023-12-01 19:11:05
 */
@Service
@Slf4j
public class ChartServiceImpl extends ServiceImpl<ChartMapper, Chart>
        implements ChartService {


    @Resource
    private RedisLimiterManager redisLimiterManager;

    //引入我们新定义的线程池
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Resource
    private BIMessageProducer biMessageProducer;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    //注入
    @Resource
    private ModelConstant modelConstant;

    @Resource
    private ChartMapper chartMapper;

    @Resource
    private AIManager aiManager;

    @Autowired
    private Map<String, ChartGenerationStrategy> strategies;

    /**
     * 🐟AI智能分析
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param loginUser
     * @return
     * @throws FileNotFoundException
     */
    @Override
    public BIResponse genChartByAiService(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) throws FileNotFoundException {

        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();

        //文件校验
        vaildFile(multipartFile);

        long biModelID = modelConstant.getModelId();

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
        String result = aiManager.doChart(biModelID, userInput.toString());

        // 将返回结果按"【【【【【"分割
        String[] splits = result.split("【【【【【");
        // 如果分割后的结果长度小于3，抛出异常
        if (splits.length < 3) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI 生成错误");
        }
        // 获取生成的图表和结果
        String genChart = splits[1].trim();
        String genResult = splits[2].trim();

        //插入到数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setName(name);
        chart.setChartType(chartType);
        chart.setGenChart(genChart);
        chart.setGenResult(genResult);
        chart.setUserId(loginUser.getId());
        int saveResult = chartMapper.insert(chart);
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
     * 星火AI模型智能分析
     * @param multipartFile
     * @param genChartByAiRequest
     * @param loginUser
     * @return
     * @throws FileNotFoundException
     */
    @Override
    public BIResponse genChartBySparkAiService(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) throws FileNotFoundException {
        // 获取当前服务器的CPU和内存使用率
        double cpuUsage = ServerMetricsUtil.getCpuUsagePercentage();
        double memoryUsage = ServerMetricsUtil.getMemoryUsagePercentage();

        // 日志记录，便于调试和监控
        log.info("CPU Usage: {}%, Memory Usage: {}%", cpuUsage, memoryUsage);

        ChartGenerationStrategy strategy;
        if (cpuUsage < 70 && memoryUsage < 70) {
            // 如果CPU和内存使用率都低于70%，使用同步策略处理
            strategy = strategies.get("synchronousStrategy");
        } else {
            // 如果资源使用高，可以返回一个提示，或者选择其他的处理策略
            strategy = strategies.get("AsynchronousStrategy");
            throw new BusinessException(ErrorCode.SYSTEM_OVERLOAD, "系统资源紧张，请稍后再试");
        }
        try {
            // 使用选定的策略生成图表
            return strategy.generateChart(multipartFile, genChartByAiRequest, loginUser);
        } catch (IOException e) {
            log.error("Error generating chart", e);
            throw new RuntimeException("图表生成失败");
        }
    }
//    @Override
//    public BIResponse genChartBySparkAiService(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) throws FileNotFoundException {
//
//        String name = genChartByAiRequest.getName();
//        String goal = genChartByAiRequest.getGoal();
//        String chartType = genChartByAiRequest.getChartType();
//
//        //文件校验
//        vaildFile(multipartFile);
//
////        long biModelID = modelConstant.getModelId();
//
//        /**
//         * 将分析需求转成代码——
//         *
//         * ——分析需求：
//         *         分析网站用户的增长情况
//         *         原始数据：
//         *         日期，用户数
//         *         1号，10
//         *         2号，20
//         *         3号，30
//         */
//        //String promote = AIManager.PRECONDITION + "分析需求 " + goal + " \n原始数据如下: " + cvsData + "\n生成图标的类型是: " + chartType;
//        //构造用户输入
//        StringBuilder userInput = new StringBuilder();
//        userInput.append(AIManager.PRECONDITION);
//        userInput.append("分析需求：").append("\n");
//        //拼接分析目标
//        String userGoal = goal;
//        if (StringUtils.isNotBlank(chartType)) {
//            userGoal += "，请使用" + chartType;
//        }
//        userInput.append(userGoal).append("\n");
//        userInput.append("原始数据:").append("\n");
//        //压缩后的数据
//        // 将excel文件转换为csv文件
//        String csvData = ExcelUtils.excelToCsv(multipartFile);
//        // 将csv文件内容添加到userInput中
//        userInput.append(csvData).append("\n");
//
//        // 调用aiManager的doChart方法，传入biModelID和userInput
//        String resultData = aiManager.sendMesToAISpark(userInput.toString());
////        String resultData = aiManager.sendMesToAISpark02(biModelID,promote);
//        log.info("AI 生成的信息: {}", resultData);
//        String[] splits = resultData.split("【【【【【");
//        ThrowUtils.throwIf(splits.length < 3, ErrorCode.SYSTEM_ERROR, "AI 生成错误");
//
//        // 获取生成的图表和结果
//        String genChart = splits[1].trim();
//        String genResult = splits[2].trim();
//
//        //插入到数据库
//        Chart chart = new Chart();
//        chart.setGoal(goal);
//        chart.setChartData(csvData);
//        chart.setName(name);
//        chart.setChartType(chartType);
//        chart.setGenChart(genChart);
//        chart.setGenResult(genResult);
////        chart.setStatus("succeed");
//        chart.setUserId(loginUser.getId());
//        int saveResult = chartMapper.insert(chart);
//        ThrowUtils.throwIf(saveResult != 1, ErrorCode.SYSTEM_ERROR, "图表保存失败");
//
//        Long chartId = chart.getId();
//
//        Chart updateChart = new Chart();
//        updateChart.setId(chartId);
//        updateChart.setStatus("succeed");
//
//        chartMapper.updateById(updateChart);
//
//        // 创建BIResponse对象
//        BIResponse biResponse = new BIResponse();
//        // 设置生成的图表和结果
//        biResponse.setGenChart(genChart);
//        biResponse.setGenResult(genResult);
//        biResponse.setChartId(chart.getId());
//        return biResponse;
//    }

    /**
     * 智能分析（异步线程池）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @return
     */
    @Override
    public BIResponse genChartByAiAsycnService(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) throws FileNotFoundException {

        String name = genChartByAiRequest.getName();
        String goal = genChartByAiRequest.getGoal();
        String chartType = genChartByAiRequest.getChartType();
        //文件校验
        vaildFile(multipartFile);


        long biModelID = modelConstant.getModelId();
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
        //压缩后的数据
        // 将excel文件转换为csv文件
        String csvData = ExcelUtils.excelToCsv(multipartFile);
        // 将csv文件内容添加到userInput中
        userInput.append(csvData).append("\n");


        //将图表插入到数据库
        Chart chart = new Chart();
        chart.setGoal(goal);
        chart.setChartData(csvData);
        chart.setName(name);
        chart.setChartType(chartType);
        //插入图表时，还没生成结束，先去掉这两个
        //chart.setGenChart(genChart);
        //chart.setGenResult(genResult);
        //设置任务状态为等待中
        chart.setStatus("wait");
        chart.setUserId(loginUser.getId());
        int saveResult = chartMapper.insert(chart);
        ThrowUtils.throwIf(saveResult <= 0, ErrorCode.SYSTEM_ERROR, "图表保存失败");

        //在最终返回结果前提交一个任务
        //todo 建议：处理任务队列满了以后抛异常的情况
        CompletableFuture.runAsync(() -> {
            //先修改图表任务状态为“”执行中”。等执行成功后，修改为“已完成”、保存执行结果；执行失败后，状态修改为“失败”，记录任务失败信息。
            Chart updateChart = new Chart();
            updateChart.setId(chart.getId());
            updateChart.setStatus("running");
            int b = chartMapper.updateById(updateChart);
            if (b <= 0) {
                handleChartUpdateError(chart.getId(), "更新图表执行中状态失败");
                return;
            }

            // 调用AI,aiManager的doChart方法，传入biModelID和userInput
            String result = aiManager.doChart(biModelID, userInput.toString());

            // 将返回结果按"【【【【【"分割
            String[] splits = result.split("【【【【【");
            // 如果分割后的结果长度小于3，抛出异常
            if (splits.length < 3) {
                handleChartUpdateError(chart.getId(), "AI 生成错误");
            }
            // 获取生成的图表和结果
            String genChart = splits[1].trim();
            String genResult = splits[2].trim();
            Chart updateChartResult = new Chart();
            updateChartResult.setId(chart.getId());
            updateChartResult.setGenChart(genChart);
            updateChartResult.setGenResult(genResult);
            updateChartResult.setStatus("succeed");
            int updateResult = chartMapper.updateById(updateChartResult);
            if (updateResult <= 0) {
                handleChartUpdateError(chart.getId(), "更新图表成功状态失败");
            }
        }, threadPoolExecutor);
        // 创建BIResponse对象
        BIResponse biResponse = new BIResponse();
        biResponse.setChartId(chart.getId());
        return biResponse;
    }

    /**
     * 智能分析（异步消息队列）
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param loginUser
     * @return
     */
    @Override
    public BIResponse genChartByAiMQService(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) throws IOException {
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
     * 图表列表信息缓存
     *
     * @return
     */
    @Override
    public List<Chart> listChartByCacheService() throws JsonProcessingException {
        /**
         * 1、首先先从缓存中查询
         * 2、如果缓存中没有，则从数据库中获取，并放入缓存，如果缓存中存在，直接返回
         * 3、如果数据库中数据进行更新，那我们的缓存也要进行同步更新
         */
        //先从缓存中查询key是否存在,从缓存中获取key值
        Set<String> keys = stringRedisTemplate.keys(CHART_LIST_CACHE_KEY);
        //判断缓存是否为空，若不为空，则从缓存中获取key中的chart表json格式数据
        //判断keys和CHART_LIST_CACHE_KEY是否相等
        boolean equals = keys.contains(CHART_LIST_CACHE_KEY);
        if (equals) {
            //从缓存中获取key中的chart表json格式数据
            String chartjson = stringRedisTemplate.opsForValue().get(CHART_LIST_CACHE_KEY);

            //用于将JSON数据转换为Java对象
            List<Chart> chartList = JSONUtil.toList(chartjson, Chart.class);

            return chartList;
        }
        //数据库中查询chartList数据
        QueryWrapper<Chart> queryWrapper = new QueryWrapper<>();
        List<Chart> chartList02 = chartMapper.selectList(queryWrapper);
        //判断chartList02不为空
        if (chartList02 == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        //将数据库中的数据进行缓存
        //1、先将Java对象转换成json格式
        ObjectMapper objectMapper01 = new ObjectMapper();
        String cachechartjson = objectMapper01.writeValueAsString(chartList02);
        //判断cachechartjson不为空
        if (cachechartjson == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        stringRedisTemplate.opsForValue().set(CHART_LIST_CACHE_KEY, cachechartjson
                , CHART_LIST_CACHE_TIME, TimeUnit.MINUTES);
        return chartList02;
    }

    /**
     * 获取单个图表缓存
     *
     * @param id
     * @return
     */
    @Override
    public Chart getChartByIdCache(long id) throws JsonProcessingException {

        //判断id是否为空
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //拼接chartCacheId
        String chartCacheId = CHART_CHCHE_ID + id;
        //从redis中获取chartCacheData
        String chartCacheData = stringRedisTemplate.opsForValue().get(chartCacheId);
        //判断chartId不为空
        if (StrUtil.isNotBlank(chartCacheData)) {
            //创建ObjectMapper对象
            ObjectMapper objectMapper = new ObjectMapper();
            //将chartCacheData转换成Chart对象
            Chart chart = objectMapper.readValue(chartCacheData, Chart.class);
            //返回成功结果
            return chart;
        }

        //根据id查询图表
        Chart chart = chartMapper.selectById(id);
        //判断图表是否为空
        if (chart == null) {
            throw new BusinessException(NOT_FOUND_ERROR);
        }
        //将图表转换成json字符串
        String chartDataJson = JSONUtil.toJsonStr(chart);
        //将图表缓存到redis中
        stringRedisTemplate.opsForValue().set(chartCacheId, chartDataJson, CHART_CACHE_TIME, TimeUnit.MINUTES);
        return chart;
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




