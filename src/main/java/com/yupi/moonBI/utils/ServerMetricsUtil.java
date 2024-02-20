package com.yupi.moonBI.utils;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import com.sun.management.OperatingSystemMXBean;

public class ServerMetricsUtil {
    private static final OperatingSystemMXBean osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private static final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

    public static double getCpuUsagePercentage() {
        return osBean.getSystemCpuLoad() * 100;
    }

    public static double getMemoryUsagePercentage() {
        long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
        long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
        return ((double) usedMemory / maxMemory) * 100;
    }
}
