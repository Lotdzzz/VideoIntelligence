package com.vi.entity.vo;

import lombok.Data;

/**
 * 这是一个接收解析后的视频集合vo
 *
 * @author dotm
 */
@Data
public class APIURLsInfoVO {

    /**
     * 视频标题
     */
    private String title;

    /**
     * 视频链接
     */
    private String url;

    /**
     * 视频封面
     */
    private String cover;
}
