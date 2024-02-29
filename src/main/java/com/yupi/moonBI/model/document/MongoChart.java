package com.yupi.moonBI.model.document;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * 图表实体类
 */
@Document("chart")
@Data
public class MongoChart {

    public static final Integer DEFAULT_VERSION = 1;
    public static final String COLLECTION_NAME = "chart";

    /**
     * MongoDB自动生成的唯一ID
     */
    @Id
    private String id;

    /**
     * 图表id
     */
    @Indexed
    @JsonSerialize(using= ToStringSerializer.class)
    private Long chartId;

    /**
     * TODO:版本号 : 用户可以更改提交的数据, 并且重新生成新的版本的图表
     */
    private Integer version;

    /**
     * 用户ID
     */
    @Indexed
    private Long userId;

    /**
     * 表名称
     */
    private String name;

    /**
     * 分析目标
     */
    private String goal;

    /**
     * wait,running,succeed,failed
     */
    private String status;

    /**
     * 执行信息
     */
    private String execMessage;
//
//    /**
//     * 图表数据
//     */
//    private String chartData;

    /**
     * 图表类型
     */
    private String chartType;

    /**
     * 生成的图表数据
     */
    private String genChart;

    /**
     * 生成的分析结论
     */
    private String genResult;

    /**
     * 创建时间
     */
    private Date createTime;

//
//    /**
//     * 更新时间
//     */
//    private Date updateTime;
//
//    /**
//     * 逻辑删除
//     */
//    private Integer isDelete;

    private static final long serialVersionUID = 1L;


    @Override
    public String toString() {
        return "Chart{" +
                "id='" + id + '\'' +
                ", chartId=" + chartId +
                ", version=" + version +
                ", userId=" + userId +
                ", name='" + name + '\'' +
                ", goal='" + goal + '\'' +
                ", status='" + status + '\'' +
                ", execMessage='" + execMessage + '\'' +
                ", chartType='" + chartType + '\'' +
                ", genChart='" + genChart + '\'' +
                ", genResult='" + genResult + '\'' +
                ", createTime=" + createTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        MongoChart mongoChart = (MongoChart) o;

        return new EqualsBuilder().append(id, mongoChart.id).append(chartId, mongoChart.chartId).append(version, mongoChart.version).append(userId, mongoChart.userId).append(name, mongoChart.name).append(goal, mongoChart.goal).append(status, mongoChart.status).append(execMessage, mongoChart.execMessage).append(chartType, mongoChart.chartType).append(genChart, mongoChart.genChart).append(genResult, mongoChart.genResult).append(createTime, mongoChart.createTime).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(id).append(chartId).append(version).append(userId).append(name).append(goal).append(status).append(execMessage).append(chartType).append(genChart).append(genResult).append(createTime).toHashCode();
    }
}
