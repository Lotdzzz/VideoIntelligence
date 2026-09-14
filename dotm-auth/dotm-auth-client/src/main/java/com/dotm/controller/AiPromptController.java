package com.dotm.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.dotm.constants.UserConstants;
import com.framework.entity.dto.AiPromptDTO;
import com.framework.entity.vo.AiPromptVO;
import com.framework.model.Result;
import com.framework.service.AiPromptService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dotm
 * AI提示词管理接口
 */
@RequestMapping("/auth/ai/prompt")
@RestController
@RequiredArgsConstructor
public class AiPromptController {

    private final AiPromptService aiPromptService;

    /**
     * 分页查询AI提示词
     * 支持搜索条件 搜索接口
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/list")
    @PreAuthorize("@ss.hasPermission('system:ai:list')")
    public Result<IPage<AiPromptVO>> list(@RequestParam(
                                                  name = UserConstants.PAGE_NUM,
                                                  defaultValue = UserConstants.DEFAULT_PAGE_NUM)
                                          Integer pageNum,
                                          @RequestParam(
                                                  name = UserConstants.PAGE_SIZE,
                                                  defaultValue = UserConstants.DEFAULT_PAGE_SIZE)
                                          Integer pageSize,
                                          AiPromptDTO aiPromptDTO) {
        //调用 Service 层分页查询AI提示词
        IPage<AiPromptVO> aiPromptPage = aiPromptService.pageAiPrompts(aiPromptDTO, pageNum, pageSize);
        //返回分页结果
        return Result.success(aiPromptPage);
    }

    /**
     * 根据ID查询AI提示词
     *
     * @param id 主键
     * @return AI提示词信息
     */
    @PreAuthorize("@ss.hasPermission('system:ai:getById')")
    @GetMapping("/{id}")
    public Result<AiPromptVO> getById(@PathVariable Long id) {
        //根据ID查询AI提示词信息
        AiPromptVO aiPromptVO = aiPromptService.getAiPromptById(id);
        return Result.success(aiPromptVO);
    }

    /**
     * 新增AI提示词
     *
     * @param dto 入参
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermission('system:ai:add')")
    @PostMapping
    public Result<Void> add(@RequestBody AiPromptDTO dto) {
        //新增AI提示词
        aiPromptService.addAiPrompt(dto);
        return Result.success();
    }

    /**
     * 修改AI提示词
     *
     * @param dto 入参
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermission('system:ai:update')")
    @PutMapping
    public Result<Void> update(@RequestBody AiPromptDTO dto) {
        //修改AI提示词
        aiPromptService.updateAiPrompt(dto);
        return Result.success();
    }

    /**
     * 删除AI提示词
     *
     * @param id 主键
     * @return 操作结果
     */
    @PreAuthorize("@ss.hasPermission('system:ai:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        //根据ID删除AI提示词
        aiPromptService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除AI提示词
     *
     * @param ids 主键集合
     * @return 操作结果
     */
    @DeleteMapping("/batchDelete")
    @PreAuthorize("@ss.hasPermission('system:ai:delete')")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        //根据ID集合批量删除AI提示词
        aiPromptService.removeByIds(ids);
        return Result.success();
    }
}