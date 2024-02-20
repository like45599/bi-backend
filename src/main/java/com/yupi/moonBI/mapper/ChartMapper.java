package com.yupi.moonBI.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yupi.moonBI.model.entity.Chart;

import java.util.List;
import java.util.Map;

/**
 * @author chenliang
 * @description 针对表【chart(图表信息表)】的数据库操作Mapper
 * @createDate 2023-12-01 19:11:05
 * @Entity com.yupi.moonBI.model.entity.Chart
 */
public interface ChartMapper extends BaseMapper<Chart> {

    List<Map<String, Object>> queryChartData(String queryString);

}




