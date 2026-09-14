package com.framework.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.framework.entity.dto.AiPromptDTO;
import com.framework.entity.model.AiPrompt;
import com.framework.entity.vo.AiPromptVO;

/**
 * @author dotm
 * @description 针对表【ai_prompt(AI提示词表)】的数据库操作Service
 */
public interface AiPromptService extends IService<AiPrompt> {

    /**
     * 分页查询AI提示词
     *
     * @param aiPromptDTO 查询入参
     * @param pageNum     当前页码
     * @param pageSize    每页条数
     * @return 分页结果
     */
    IPage<AiPromptVO> pageAiPrompts(AiPromptDTO aiPromptDTO, long pageNum, long pageSize);

    /**
     * 根据ID查询AI提示词
     *
     * @param id 主键
     * @return AI提示词信息
     */
    AiPromptVO getAiPromptById(Long id);

    /**
     * 新增AI提示词
     *
     * @param dto 入参
     * @return 是否成功
     */
    boolean addAiPrompt(AiPromptDTO dto);

    /**
     * 修改AI提示词
     *
     * @param dto 入参
     * @return 是否成功
     */
    boolean updateAiPrompt(AiPromptDTO dto);

    /**
     * 根据key获取提示词
     *
     * @param key AI对话提示词唯一标识
     * @return AI提示词信息
     */
    AiPromptVO getAiPromptByKey(String key);
}