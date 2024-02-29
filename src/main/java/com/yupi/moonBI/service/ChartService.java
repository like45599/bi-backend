package com.yupi.moonBI.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.yupi.moonBI.model.dto.chart.ChartQueryRequest;
import com.yupi.moonBI.model.dto.chart.GenChartByAiRequest;
import com.yupi.moonBI.model.entity.Chart;
import com.yupi.moonBI.model.entity.User;
import com.yupi.moonBI.model.vo.BIResponse;
import org.springframework.data.domain.Page;
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


    /**
     * MongoDB存储图表数据   不包含更新版本号功能
     */

    /**
     * 保存chart文档 : 当存在旧版本时自动设置为newVersion
     *
     * @param chart 图表
     * @return boolean
     */
    boolean saveDocument(com.yupi.moonBI.model.document.Chart chart);

    /**
     * 同步com.yupi.moonBI.model.document.Chart数据到MongoDB
     *
     * @param chartEntity 表实体
     * @return boolean
     */
    boolean syncChart(Chart chartEntity, String genChart ,String genResult);

    /**
     * 列表文件
     *
     * @param userId 用户id
     * @return {@link List}<{@link com.yupi.moonBI.model.document.Chart}>
     */
//    List<com.yupi.moonBI.model.document.Chart> listDocuments(long userId);

    /**
     * 查询图表Document
     *
     * @param chartQueryRequest 图查询请求
     * @return {@link Page}<{@link com.yupi.moonBI.model.document.Chart}>
     */
    Page<com.yupi.moonBI.model.document.Chart> getChartList(ChartQueryRequest chartQueryRequest);

    /**
     * 通过com.yupi.moonBI.model.document.ChartId 获取 com.yupi.moonBI.model.document.Chart(latest version)
     *
     * @param chartId 表id
     * @return {@link com.yupi.moonBI.model.document.Chart}
     */
    com.yupi.moonBI.model.document.Chart getChartByChartId(long chartId);

    /**
     * 插入com.yupi.moonBI.model.document.Chart
     *
     * @param chartEntity 表实体
     * @return boolean
     */
    boolean insertChart(Chart chartEntity);

    /**
     * 从mongo删除com.yupi.moonBI.model.document.Chart
     *
     * @param id id
     * @return boolean
     */
    boolean deleteAllFromMongo(long id);


    /**
     * 从mongo更新com.yupi.moonBI.model.document.Chart : 创建新的版本
     *
     * @param chart 图表
     * @return boolean
     */
    boolean updateDocument(com.yupi.moonBI.model.document.Chart chart);
}
