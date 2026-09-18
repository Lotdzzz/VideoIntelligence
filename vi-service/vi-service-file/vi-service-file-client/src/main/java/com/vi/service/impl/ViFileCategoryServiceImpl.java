package com.vi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.framework.exception.ServiceException;
import com.framework.utils.StringUtils;
import com.vi.constants.FileConstants;
import com.vi.entity.dto.ViFileCategoryDTO;
import com.vi.entity.model.ViFileCategory;
import com.vi.entity.vo.ViFileCategoryVO;
import com.vi.mapper.ViFileCategoryMapper;
import com.vi.service.ViFileCategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author dotm
 * 针对表【vi_file_category(资源文件分类表)】的数据库操作Service实现
 * 创建时间：2026-09-18 10:00:00
 */
@Service
public class ViFileCategoryServiceImpl extends ServiceImpl<ViFileCategoryMapper, ViFileCategory>
        implements ViFileCategoryService {

    /**
     * 分页查询资源文件分类
     */
    @Override
    public IPage<ViFileCategoryVO> pageCategories(ViFileCategoryDTO viFileCategoryDTO, long pageNum, long pageSize) {
        //创建分页对象
        Page<ViFileCategory> page = new Page<>(pageNum, pageSize);

        //执行分页查询
        IPage<ViFileCategory> categoryPage = page(page, buildQueryWrapper(viFileCategoryDTO));

        //转换为VO分页结果
        Page<ViFileCategoryVO> voPage = new Page<>(pageNum, pageSize, categoryPage.getTotal());
        voPage.setRecords(categoryPage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    /**
     * 查询资源文件分类列表（不分页，用于下拉选择）
     */
    @Override
    public List<ViFileCategoryVO> listCategories(ViFileCategoryDTO viFileCategoryDTO) {
        //按条件查询分类列表
        List<ViFileCategory> categoryList = list(buildQueryWrapper(viFileCategoryDTO));
        //转换为VO列表
        return categoryList.stream().map(this::toVO).toList();
    }

    /**
     * 根据分类ID查询资源文件分类
     */
    @Override
    public ViFileCategoryVO getCategoryById(Long id) {
        //根据ID查询分类实体
        ViFileCategory viFileCategory = getById(id);
        //转换为VO
        return toVO(viFileCategory);
    }

    /**
     * 新增资源文件分类
     */
    @Override
    public boolean addCategory(ViFileCategoryDTO dto) {
        //校验同一用户下的分类名称是否重复
        checkCategoryNameUnique(dto.getUserId(), dto.getCategoryName(), null);
        //DTO转换为实体（忽略id，由数据库自增生成）
        ViFileCategory viFileCategory = new ViFileCategory();
        BeanUtils.copyProperties(dto, viFileCategory, "id");
        //保存
        return save(viFileCategory);
    }

    /**
     * 修改资源文件分类
     */
    @Override
    public boolean updateCategory(ViFileCategoryDTO dto) {
        //校验同一用户下的分类名称是否重复（排除自身）
        checkCategoryNameUnique(dto.getUserId(), dto.getCategoryName(), dto.getId());
        //DTO转换为实体
        ViFileCategory viFileCategory = new ViFileCategory();
        BeanUtils.copyProperties(dto, viFileCategory);
        //根据ID更新
        return updateById(viFileCategory);
    }

    /**
     * 构建分类查询条件
     *
     * @param viFileCategoryDTO 查询条件
     * @return 条件构造器
     */
    private LambdaQueryWrapper<ViFileCategory> buildQueryWrapper(ViFileCategoryDTO viFileCategoryDTO) {
        //条件构造器
        LambdaQueryWrapper<ViFileCategory> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(
                viFileCategoryDTO.getUserId() != null,
                ViFileCategory::getUserId,
                viFileCategoryDTO.getUserId()
        );

        wrapper.like(
                StringUtils.isNotEmpty(viFileCategoryDTO.getCategoryName()),
                ViFileCategory::getCategoryName,
                viFileCategoryDTO.getCategoryName()
        );

        wrapper.between(
                viFileCategoryDTO.getBeginTime() != null && viFileCategoryDTO.getEndTime() != null,
                ViFileCategory::getCreateTime,
                viFileCategoryDTO.getBeginTime(),
                viFileCategoryDTO.getEndTime()
        );

        //按排序值、分类ID升序
        wrapper.orderByAsc(ViFileCategory::getSort, ViFileCategory::getId);

        return wrapper;
    }

    /**
     * 校验同一用户下的分类名称是否重复
     *
     * @param userId       所属用户ID
     * @param categoryName 分类名称
     * @param excludeId    需要排除的分类ID（修改时传入自身ID）
     */
    private void checkCategoryNameUnique(Long userId, String categoryName, Long excludeId) {
        //用户ID或分类名称为空时不校验
        if (userId == null || StringUtils.isEmpty(categoryName)) {
            return;
        }

        LambdaQueryWrapper<ViFileCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViFileCategory::getUserId, userId);
        wrapper.eq(ViFileCategory::getCategoryName, categoryName);
        wrapper.ne(excludeId != null, ViFileCategory::getId, excludeId);

        if (count(wrapper) > 0) {
            throw new ServiceException(FileConstants.CATEGORY_NAME_EXISTS);
        }
    }

    /**
     * 分类实体转换为视图对象
     *
     * @param viFileCategory 分类实体
     * @return 分类视图对象
     */
    private ViFileCategoryVO toVO(ViFileCategory viFileCategory) {
        if (viFileCategory == null) {
            return null;
        }
        ViFileCategoryVO vo = new ViFileCategoryVO();
        BeanUtils.copyProperties(viFileCategory, vo);
        return vo;
    }

    /**
     * 根据用户id查询分类
     */
    @Override
    public List<ViFileCategoryVO> listCategoriesByUserId(Long userId) {
        LambdaQueryWrapper<ViFileCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ViFileCategory::getUserId, userId);
        List<ViFileCategory> categoryList = list(wrapper);
        if (categoryList != null && !categoryList.isEmpty()) {
            return categoryList.stream().map(this::toVO).toList();
        }
        return List.of();
    }
}
