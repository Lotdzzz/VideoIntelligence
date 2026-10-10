package com.vi.comtroller;

import com.framework.model.Result;
import com.vi.consumer.AnalysisResultConsumer;
import com.vi.entity.dto.ViFileDTO;
import com.vi.entity.model.AnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dotm
 * 分析结果的控制器
 */
@RestController
@RequestMapping("/analysis")
@RequiredArgsConstructor
@Slf4j
public class AnalysisResultController {

    @GetMapping("/result/{userId}/{taskId}")
    public Result<Object> getAnalysisResult(@PathVariable String userId, @PathVariable String taskId) {

        for (AnalysisResult analysisResult : AnalysisResultConsumer.analysisResults) {
            ViFileDTO data = analysisResult.getData();
            if (data.getId() == null || data.getUserId() == null) {
                log.warn("Analysis result data is missing userId or taskId: {}", data);
                continue;
            }
            if (data.getUserId().toString().equals(userId) &&
                    data.getId().toString().equals(taskId)) {
                log.info("Found analysis result for user: {} and task: {} 结果： {}", userId, taskId, analysisResult);
                return Result.success(analysisResult);
            }
        }
        return Result.success();
    }
}
