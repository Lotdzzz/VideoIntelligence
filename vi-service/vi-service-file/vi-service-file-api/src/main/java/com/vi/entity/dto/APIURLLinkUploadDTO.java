package com.vi.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 链接上传dto
 *
 * @author dotm
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class APIURLLinkUploadDTO {

    /**
     * 视频链接
     */
    private String url;
}
