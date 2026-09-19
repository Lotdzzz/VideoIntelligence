package com.vi.controller.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.framework.model.Result;
import com.framework.utils.SecurityUtils;
import com.vi.constants.FileConstants;
import com.vi.entity.dto.ViFileCategoryDTO;
import com.vi.entity.vo.ViFileCategoryVO;
import com.vi.service.ViFileCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端分类接口
 *
 * @author dotm
 */
@RestController
@RequestMapping("/file/user/category")
@RequiredArgsConstructor
public class FileCategoryController {

    private final ViFileCategoryService viFileCategoryService;

    /**
     * 分页查询资源文件分类
     * 支持搜索条件 搜索接口
     *
     * @param pageNum  当前页码
     * @param pageSize 每页条数
     * @return 分页结果
     */
    @GetMapping("/list")
    public Result<IPage<ViFileCategoryVO>> list(@RequestParam(
                                                        name = FileConstants.PAGE_NUM,
                                                        defaultValue = FileConstants.DEFAULT_PAGE_NUM)
                                                Integer pageNum,
                                                @RequestParam(
                                                        name = FileConstants.PAGE_SIZE,
                                                        defaultValue = FileConstants.DEFAULT_PAGE_SIZE)
                                                Integer pageSize,
                                                ViFileCategoryDTO viFileCategoryDTO) {
        //设置用户id
        viFileCategoryDTO.setUserId(SecurityUtils.getUserId());

        //调用 Service 层分页查询资源文件分类
        IPage<ViFileCategoryVO> categoryPage = viFileCategoryService.pageCategories(viFileCategoryDTO, pageNum, pageSize);
        //返回分页结果
        return Result.success(categoryPage);
    }

    /**
     * 查询资源文件分类列表（不分页，用于下拉选择）
     *
     * @param viFileCategoryDTO 查询条件
     * @return 分类列表
     */
    @GetMapping("/all")
    public Result<List<ViFileCategoryVO>> listAll(ViFileCategoryDTO viFileCategoryDTO) {
        viFileCategoryDTO.setUserId(SecurityUtils.getUserId());
        //查询资源文件分类列表
        List<ViFileCategoryVO> categoryList = viFileCategoryService.listCategories(viFileCategoryDTO);
        return Result.success(categoryList);
    }

    /**
     * 根据分类ID查询资源文件分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    @GetMapping("/{id}")
    public Result<ViFileCategoryVO> getById(@PathVariable Long id) {
        //根据分类ID查询分类信息
        ViFileCategoryVO viFileCategoryVO = viFileCategoryService.getCategoryById(id);
        return Result.success(viFileCategoryVO);
    }

    /**
     * 新增资源文件分类
     *
     * @param dto 入参
     * @return 操作结果
     */
    @PostMapping
    public Result<Void> add(@RequestBody ViFileCategoryDTO dto) {
        //新增资源文件分类
        viFileCategoryService.addCategory(dto);
        return Result.success();
    }

    /**
     * 修改资源文件分类
     *
     * @param dto 入参
     * @return 操作结果
     */
    @PutMapping
    public Result<Void> update(@RequestBody ViFileCategoryDTO dto) {
        //修改资源文件分类
        viFileCategoryService.updateCategory(dto);
        return Result.success();
    }

    /**
     * 删除资源文件分类
     *
     * @param id 分类ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        //根据分类ID删除分类
        viFileCategoryService.removeById(id);
        return Result.success();
    }

    /**
     * 批量删除资源文件分类
     *
     * @param ids 分类ID集合
     * @return 操作结果
     */
    @DeleteMapping("/batchDelete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        //根据分类ID集合批量删除分类
        viFileCategoryService.removeByIds(ids);
        return Result.success();
    }

    /**
     * 根据用户id查询分类
     *
     * @return 分类列表
     */
    @GetMapping("/user")
    public Result<List<ViFileCategoryVO>> listByUserId() {
        List<ViFileCategoryVO> categoryList = viFileCategoryService.listCategoriesByUserId(SecurityUtils.getUserId());
        return Result.success(categoryList);
    }
}
