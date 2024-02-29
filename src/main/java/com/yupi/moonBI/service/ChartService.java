package com.yupi.moonBI.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yupi.moonBI.model.dto.chart.GenChartByAiRequest;
import com.yupi.moonBI.model.entity.Chart;
import com.yupi.moonBI.model.entity.User;
import com.yupi.moonBI.model.vo.BIResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * @author chenliang
 * @description 针对表【chart(图表信息表)】的数据库操作Service
 * @createDate 2023-12-01 19:11:05
 */
public interface ChartService extends IService<Chart> {

    /**
     * 智能数据分析生成
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param loginUser
     * @return
     * @throws FileNotFoundException
     */
    BIResponse genChartByAiService(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) throws FileNotFoundException;

    /**
     * 星火AI模型智能分析
     *
     * 策略模式生成图表
     *
     * @param multipartFile
     * @param genChartByAiRequest
     * @param loginUser
     * @return
     * @throws FileNotFoundException
     */
    BIResponse genChartBySparkAiService(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) throws FileNotFoundException;

    /**
     * 智能分析（异步线程池）
     * @param multipartFile
     * @param genChartByAiRequest
     * @param loginUser
     * @return
     */
    BIResponse genChartByAiAsycnService(MultipartFile multipartFile,
                                        GenChartByAiRequest genChartByAiRequest,User loginUser) throws FileNotFoundException;

    /**
     * 智能分析（异步消息队列）
     * @param multipartFile
     * @param genChartByAiRequest
     * @param loginUser
     * @return
     */
    BIResponse genChartByAiMQService(MultipartFile multipartFile,
                                        GenChartByAiRequest genChartByAiRequest,User loginUser) throws IOException;

    /**
     * 图表列表信息缓存
     * @return
     */
    List<Chart> listChartByCacheService() throws JsonProcessingException;

    /**
     * 获取单个图表缓存
     * @param id
     * @return
     */
    Chart getChartByIdCache(long id) throws JsonProcessingException;

    /**
     * 策略模式生成图表
     * @param multipartFile
     * @param genChartByAiRequest
     * @param loginUser
     * @return
     * @throws FileNotFoundException
     */

}
