package com.vi.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.framework.model.Result;
import com.vi.constants.FileConstants;
import com.vi.entity.dto.ViFileDTO;
import com.vi.entity.vo.ViFileVO;
import com.vi.service.ViFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author dotm
 * VI资源文件管理接口
 */
@RequestMapping("/file/resource")
@RestController
@RequiredArgsConstructor
public class ViFileController {

    private final ViFileService viFileService;

    /**
     * 分页查询资源文件
     * 支持搜索条件 搜索接口
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<IPage<ViFileVO>> list(@RequestParam(
                                                name = FileConstants.PAGE_NUM,
                                                defaultValue = FileConstants.DEFAULT_PAGE_NUM)
                                        Integer pageNum,
                                        @RequestParam(
                                                name = FileConstants.PAGE_SIZE,
                                                defaultValue = FileConstants.DEFAULT_PAGE_SIZE)
                                        Integer pageSize,
                                        ViFileDTO viFileDTO) {
        //调用 Service 层分页查询资源文件
        IPage<ViFileVO> filePage = viFileService.pageFiles(viFileDTO, pageNum, pageSize);
        //返回分页结果
        return Result.success(filePage);
    }

    /**
     * 根据文件ID查询资源文件
     *
     * @param id 文件ID
     * @return 文件信息
     */
    @GetMapping("/{id}")
    public Result<ViFileVO> getById(@PathVariable Long id) {
        //根据文件ID查询文件信息
        ViFileVO viFileVO = viFileService.getFileById(id);
        return Result.success(viFileVO);
    }

    /**
     * 新增资源文件
     *
     * @param dto 入参
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> add(@RequestBody ViFileDTO dto) {
        //新增资源文件
        viFileService.addFile(dto);
        return Result.success();
    }

    /**
     * 修改资源文件
     *
     * @param dto 入参
     * @return 操作结果
     */
    @PutMapping
    public Result<Void> update(@RequestBody ViFileDTO dto) {
        //修改资源文件
        viFileService.updateFile(dto);
        return Result.success();
    }

    /**
     * 删除资源文件
     *
     * @param id 文件ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        //根据文件ID删除文件
        viFileService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除资源文件
     *
     * @param ids 文件ID集合
     * @return 操作结果
     */
    @DeleteMapping("/batchDelete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        //根据文件ID集合批量删除文件
        viFileService.removeByIds(ids);
        return Result.success();
    }
}
