package com.vi.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.spring.service.IService;
import com.vi.entity.dto.ViFileDTO;
import com.vi.entity.model.ViFile;
import com.vi.entity.vo.ViFileVO;

/**
* @author dotm
* @description 针对表【vi_file(资源文件表)】的数据库操作Service
* @createDate 2026-09-18 10:00:00
*/
public interface ViFileService extends IService<ViFile> {

    /**
     * 分页查询资源文件
     *
     * @param viFileDTO 查询条件
     * @param pageNum   当前页码
     * @param pageSize  每页条数
     * @return 分页结果
     */
    IPage<ViFileVO> pageFiles(ViFileDTO viFileDTO, long pageNum, long pageSize);

    /**
     * 根据文件ID查询资源文件
     *
     * @param id 文件ID
     * @return 文件信息
     */
    ViFileVO getFileById(Long id);

    /**
     * 新增资源文件
     *
     * @param dto 入参
     * @return 是否成功
     */
    boolean addFile(ViFileDTO dto);

    /**
     * 修改资源文件
     *
     * @param dto 入参
     * @return 是否成功
     */
    boolean updateFile(ViFileDTO dto);
}
