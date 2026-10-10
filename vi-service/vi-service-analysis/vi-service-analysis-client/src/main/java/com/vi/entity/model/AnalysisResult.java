package com.vi.entity.model;

import com.vi.entity.dto.ViFileDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 消费者消息模型
 *
 * @author dotm
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AnalysisResult {

    private Object knowledge;

    private Map<String, Object> mind;

    private ViFileDTO data;

}
