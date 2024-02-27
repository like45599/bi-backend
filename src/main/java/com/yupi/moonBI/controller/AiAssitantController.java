package com.yupi.moonBI.controller;

import com.yupi.moonBI.common.BaseResponse;
import com.yupi.moonBI.common.ErrorCode;
import com.yupi.moonBI.common.ResultUtils;
import com.yupi.moonBI.exception.BusinessException;
import com.yupi.moonBI.exception.ThrowUtils;
import com.yupi.moonBI.manager.AIManager;
import com.yupi.moonBI.manager.RedisLimiterManager;
import com.yupi.moonBI.model.dto.aiassistant.GenChatByAiRequest;
import com.yupi.moonBI.model.entity.AiAssistant;
import com.yupi.moonBI.model.entity.User;
import com.yupi.moonBI.service.AiAssistantService;
import com.yupi.moonBI.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/aiAssistant")
@Slf4j
@CrossOrigin(origins = "http://123.57.241.179:80", allowCredentials = "true")

//@CrossOrigin(origins = "http://140.143.151.205:80", allowCredentials = "true")
public class AiAssitantController {

    @Resource
    private UserService userService;

    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Resource
    private AIManager aiManager;

    @Resource
    private AiAssistantService aiAssistantService;


    @PostMapping("/chat")
    public BaseResponse<?> aiAssistant(@RequestBody GenChatByAiRequest genChatByAiRequest, HttpServletRequest request) {

        String questionName = genChatByAiRequest.getQuestionName();
        String questionGoal = genChatByAiRequest.getQuestionGoal();
        String questionType = genChatByAiRequest.getQuestionType();
        User loginUser = userService.getLoginUser(request);
        // 校验
        if (StringUtils.isBlank(questionName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "问题名称为空");
        }

        if (ObjectUtils.isEmpty(questionType)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "问题类型为空");
        }

        if (StringUtils.isBlank(questionGoal)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "问题分析目标为空");
        }

        // 用户每秒限流
        redisLimiterManager.doRateLimit("Ai_Rate_" + loginUser.getId());


        long aiModelId = 1654785040361893889L;

//        String result = aiManager.doAiChat(aiModelId, questionGoal);
        String result = aiManager.sendMesToAISpark(questionGoal);

        AiAssistant aiAssistant = new AiAssistant();
        aiAssistant.setQuestionName(questionName);
        aiAssistant.setQuestionGoal(questionGoal);
        aiAssistant.setQuestionType(questionType);
        aiAssistant.setUserId(loginUser.getId());
        aiAssistant.setQuestionResult(result);
        aiAssistant.setQuestionStatus("succeed");
        boolean saveResult = aiAssistantService.save(aiAssistant);
        ThrowUtils.throwIf(!saveResult,ErrorCode.SYSTEM_ERROR,"ai对话保存失败");

//        System.out.println(result);
        return ResultUtils.success(aiAssistant);
    }
}
