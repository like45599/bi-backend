package com.yupi.moonBI.service.impl;

import com.yupi.moonBI.manager.AIManager;
import com.yupi.moonBI.service.SparkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
@Service
@Slf4j
public class SparkServiceImpl implements SparkService {

    @Resource
    private AIManager  aiManager;

    /**
     *  调用spark ai接口发送问题
     * @param question
     * @return
     */
    @Override
    public String sparkAIsendQuestion(String question) {

        String answer = aiManager.sendMesToAISpark(question);
        log.info("spark ai返回结果："+answer);
        return answer;
    }

}
