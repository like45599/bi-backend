package com.yupi.moonBI.manager;

import com.yupi.moonBI.common.ErrorCode;
import com.yupi.moonBI.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * 专门提供RedisLimiter限流基础服务的管理器
 * 提供了通用的能力，可以放到其他项目中使用
 *
 * @author chenliang
 */
@Service
@Slf4j
public class RedisLimiterManager {

    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流
     *
     * @param key 区分不同的限流器，比如不同的用户id应该分别统计
     */
    public void doRateLimit(String key) {
        //创建一个限流器
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 限流规则：设置 overall 的限流大小为 2，限流时间间隔为 1 秒
        //RateType.OVERALL表示速率限制作用于整个令牌桶，即限制所有请求的速率
        rateLimiter.trySetRate(RateType.OVERALL, 2, 1, RateIntervalUnit.SECONDS);
        // 尝试获取令牌，如果获取失败则抛出异常
        boolean tryAcquire = rateLimiter.tryAcquire(1);
        if (!tryAcquire) {
            log.debug("请求太频繁，被限流了哦！");
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
