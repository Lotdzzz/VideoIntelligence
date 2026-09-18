package com.vi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.vi.entity.dto.ViFileCategoryDTO;
import com.vi.entity.model.ViFileCategory;
import com.vi.entity.vo.ViFileCategoryVO;

import java.util.List;

/**
* @author dotm
* @description 针对表【vi_file_category(资源文件分类表)】的数据库操作Service
* @createDate 2026-09-18 10:00:00
*/
public interface ViFileCategoryService extends IService<ViFileCategory> {

    /**
     * 分页查询资源文件分类
     *
     * @param viFileCategoryDTO 查询条件
     * @param pageNum           当前页码
     * @param pageSize          每页条数
     * @return 分页结果
     */
    IPage<ViFileCategoryVO> pageCategories(ViFileCategoryDTO viFileCategoryDTO, long pageNum, long pageSize);

    /**
     * 查询资源文件分类列表（不分页，用于下拉选择）
     *
     * @param viFileCategoryDTO 查询条件
     * @return 分类列表
     */
    List<ViFileCategoryVO> listCategories(ViFileCategoryDTO viFileCategoryDTO);

    /**
     * 根据分类ID查询资源文件分类
     *
     * @param id 分类ID
     * @return 分类信息
     */
    ViFileCategoryVO getCategoryById(Long id);

    /**
     * 新增资源文件分类
     *
     * @param dto 入参
     * @return 是否成功
     */
    boolean addCategory(ViFileCategoryDTO dto);

    /**
     * 修改资源文件分类
     *
     * @param dto 入参
     * @return 是否成功
     */
    boolean updateCategory(ViFileCategoryDTO dto);

    /**
     * 根据用户id查询分类
     */
    List<ViFileCategoryVO> listCategoriesByUserId(Long userId);
}
