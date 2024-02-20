package com.yupi.moonBI.strategy;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yupi.moonBI.common.ErrorCode;
import com.yupi.moonBI.exception.BusinessException;
import com.yupi.moonBI.exception.ThrowUtils;
import com.yupi.moonBI.manager.AIManager;
import com.yupi.moonBI.model.dto.chart.GenChartByAiRequest;
import com.yupi.moonBI.model.entity.Chart;
import com.yupi.moonBI.model.entity.User;
import com.yupi.moonBI.model.vo.BIResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
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
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static com.yupi.moonBI.common.ErrorCode.NOT_FOUND_ERROR;
import static com.yupi.moonBI.constant.RedisConstant.*;
import static com.yupi.moonBI.constant.RedisConstant.CHART_CACHE_TIME;

public interface ChartGenerationStrategy {
    BIResponse generateChart(MultipartFile multipartFile, GenChartByAiRequest genChartByAiRequest, User loginUser) throws IOException;
}

