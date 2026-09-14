package com.framework.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.framework.entity.dto.AiPromptDTO;
import com.framework.entity.model.AiPrompt;
import com.framework.entity.vo.AiPromptVO;
import com.framework.mapper.AiPromptMapper;
import com.framework.service.AiPromptService;
import com.framework.utils.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author dotm
 * @description 针对表【ai_prompt(AI提示词表)】的数据库操作Service实现
 */
@Service
@RequiredArgsConstructor
public class AiPromptServiceImpl extends ServiceImpl<AiPromptMapper, AiPrompt>
        implements AiPromptService {

    /**
     * 分页查询AI提示词
     */
    @Override
    public IPage<AiPromptVO> pageAiPrompts(AiPromptDTO aiPromptDTO, long pageNum, long pageSize) {
        //创建分页对象
        Page<AiPrompt> page = new Page<>(pageNum, pageSize);

        //条件构造器
        LambdaQueryWrapper<AiPrompt> wrapper = new LambdaQueryWrapper<>();

        wrapper.like(
                StringUtils.isNotEmpty(aiPromptDTO.getName()),
                AiPrompt::getName,
                aiPromptDTO.getName()
        );

        wrapper.like(
                StringUtils.isNotEmpty(aiPromptDTO.getModel()),
                AiPrompt::getModel,
                aiPromptDTO.getModel()
        );

        wrapper.eq(
                aiPromptDTO.getStatus() != null,
                AiPrompt::getStatus,
                aiPromptDTO.getStatus()
        );

        //执行分页查询
        IPage<AiPrompt> aiPromptPage = page(page, wrapper);

        //转换为VO分页结果
        Page<AiPromptVO> voPage = new Page<>(pageNum, pageSize, aiPromptPage.getTotal());
        voPage.setRecords(aiPromptPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    /**
     * 根据ID查询AI提示词
     */
    @Override
    public AiPromptVO getAiPromptById(Long id) {
        //根据ID查询实体
        AiPrompt aiPrompt = getById(id);
        //转换为VO
        return toVO(aiPrompt);
    }

    /**
     * 新增AI提示词
     */
    @Override
    public boolean addAiPrompt(AiPromptDTO dto) {
        //DTO转换为实体（忽略id，由数据库自增生成）
        AiPrompt aiPrompt = new AiPrompt();
        BeanUtils.copyProperties(dto, aiPrompt, "id");
        //保存
        return save(aiPrompt);
    }

    /**
     * 修改AI提示词
     */
    @Override
    public boolean updateAiPrompt(AiPromptDTO dto) {
        //DTO转换为实体
        AiPrompt aiPrompt = new AiPrompt();
        BeanUtils.copyProperties(dto, aiPrompt);
        //根据ID更新
        return updateById(aiPrompt);
    }

    /**
     * 实体转换为视图对象
     *
     * @param aiPrompt 实体
     * @return 视图对象
     */
    private AiPromptVO toVO(AiPrompt aiPrompt) {
        if (aiPrompt == null) {
            return null;
        }
        AiPromptVO vo = new AiPromptVO();
        BeanUtils.copyProperties(aiPrompt, vo);
        return vo;
    }

    /**
     * 根据key获取提示词
     *
     * @param key AI对话提示词唯一标识
     * @return AI提示词信息
     */
    @Override
    public AiPromptVO getAiPromptByKey(String key) {
        LambdaQueryWrapper<AiPrompt> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiPrompt::getKey, key);
        AiPrompt aiPrompt = getOne(wrapper);
        if (aiPrompt != null) {
            return toVO(aiPrompt);
        }
        return null;
    }
}