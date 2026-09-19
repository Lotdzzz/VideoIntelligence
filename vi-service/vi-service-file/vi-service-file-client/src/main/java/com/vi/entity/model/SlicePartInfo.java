package com.vi.entity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 具体分片的存储信息
 *
 * @author dotm
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SlicePartInfo {

    /**
     * 分片标识
     */
    private Integer partNumber;

    /**
     * MinIO 返回的 ETag
     */
    private String eTag;
}
