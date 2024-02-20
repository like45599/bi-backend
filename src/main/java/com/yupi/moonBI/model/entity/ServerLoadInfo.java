package com.yupi.moonBI.model.entity;

import lombok.Data;

@Data
public class ServerLoadInfo {
    private double cpuUsagePercentage;
    private double memoryUsagePercentage;

    public ServerLoadInfo(double cpuUsagePercentage, double memoryUsagePercentage) {
        this.cpuUsagePercentage = cpuUsagePercentage;
        this.memoryUsagePercentage = memoryUsagePercentage;
    }

    // 根据负载情况定义方法判断负载级别
    public boolean isVeryHighLoad() {
        return cpuUsagePercentage > 80 || memoryUsagePercentage > 80;
    }

    public boolean isHighLoad() {
        return cpuUsagePercentage > 70 || memoryUsagePercentage > 70;
    }

    public boolean isMediumLoad() {
        return cpuUsagePercentage > 60 || memoryUsagePercentage > 60;
    }

    // Getters
}
