package com.yupi.moonBI.mapper;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@SpringBootTest
class ChartMapperTest {

    @Resource
    private ChartMapper chartMapper;

    @Test
    void queryChartData() {
        String chartId = "1731603236786847745";
        String querysql = String.format("select 用户数 from chart_%s where 日期=1", chartId);
        List<Map<String, Object>> chartData = chartMapper.queryChartData(querysql);
        System.out.println(chartData);
    }
}