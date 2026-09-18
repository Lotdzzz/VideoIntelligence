package com.vi.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.framework.utils.StringUtils;
import com.vi.entity.dto.ViFileDTO;
import com.vi.entity.model.ViFile;
import com.vi.entity.vo.ViFileVO;
import com.vi.mapper.ViFileMapper;
import com.vi.service.ViFileService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
* @author dotm
* 针对表【vi_file(资源文件表)】的数据库操作Service实现
* 创建时间：2026-09-18 10:00:00
*/
@Service
public class ViFileServiceImpl extends ServiceImpl<ViFileMapper, ViFile>
    implements ViFileService {

    /**
     * 分页查询资源文件
     */
    @Override
    public IPage<ViFileVO> pageFiles(ViFileDTO viFileDTO, long pageNum, long pageSize) {
        //创建分页对象
        Page<ViFile> page = new Page<>(pageNum, pageSize);

        //条件构造器
        LambdaQueryWrapper<ViFile> wrapper = new LambdaQueryWrapper<>();

        wrapper.eq(
                viFileDTO.getUserId() != null,
                ViFile::getUserId,
                viFileDTO.getUserId()
        );

        wrapper.like(
                StringUtils.isNotEmpty(viFileDTO.getOriginalName()),
                ViFile::getOriginalName,
                viFileDTO.getOriginalName()
        );

        wrapper.eq(
                StringUtils.isNotEmpty(viFileDTO.getFileType()),
                ViFile::getFileType,
                viFileDTO.getFileType()
        );

        wrapper.eq(
                StringUtils.isNotEmpty(viFileDTO.getFileExt()),
                ViFile::getFileExt,
                viFileDTO.getFileExt()
        );

        wrapper.eq(
                viFileDTO.getCategoryId() != null,
                ViFile::getCategoryId,
                viFileDTO.getCategoryId()
        );

        wrapper.eq(
                viFileDTO.getStatus() != null,
                ViFile::getStatus,
                viFileDTO.getStatus()
        );

        wrapper.like(
                StringUtils.isNotEmpty(viFileDTO.getMd5()),
                ViFile::getMd5,
                viFileDTO.getMd5()
        );

        wrapper.between(
                viFileDTO.getBeginTime() != null && viFileDTO.getEndTime() != null,
                ViFile::getCreateTime,
                viFileDTO.getBeginTime(),
                viFileDTO.getEndTime()
        );

        //按创建时间倒序
        wrapper.orderByDesc(ViFile::getCreateTime);

        //执行分页查询
        IPage<ViFile> filePage = page(page, wrapper);

        //转换为VO分页结果
        Page<ViFileVO> voPage = new Page<>(pageNum, pageSize, filePage.getTotal());
        voPage.setRecords(filePage.getRecords().stream().map(this::toVO).toList());
        return voPage;
    }

    /**
     * 根据文件ID查询资源文件
     */
    @Override
    public ViFileVO getFileById(Long id) {
        //根据ID查询文件实体
        ViFile viFile = getById(id);
        //转换为VO
        return toVO(viFile);
    }

    /**
     * 新增资源文件
     */
    @Override
    public boolean addFile(ViFileDTO dto) {
        //DTO转换为实体（忽略id，由数据库自增生成）
        ViFile viFile = new ViFile();
        BeanUtils.copyProperties(dto, viFile, "id");
        //保存
        return save(viFile);
    }

    /**
     * 修改资源文件
     */
    @Override
    public boolean updateFile(ViFileDTO dto) {
        //DTO转换为实体
        ViFile viFile = new ViFile();
        BeanUtils.copyProperties(dto, viFile);
        //根据ID更新
        return updateById(viFile);
    }

    /**
     * 文件实体转换为视图对象
     *
     * @param viFile 文件实体
     * @return 文件视图对象
     */
    private ViFileVO toVO(ViFile viFile) {
        if (viFile == null) {
            return null;
        }
        ViFileVO vo = new ViFileVO();
        BeanUtils.copyProperties(viFile, vo);
        return vo;
    }
}
