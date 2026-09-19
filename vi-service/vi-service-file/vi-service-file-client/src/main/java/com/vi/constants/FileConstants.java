package com.vi.constants;

/**
 * @author dotm
 * VI资源文件模块常量信息
 */
public class FileConstants {

    /**
     * 分页
     */
    public static final String DEFAULT_PAGE_NUM = "1";
    public static final String DEFAULT_PAGE_SIZE = "36";
    public static final String PAGE_NUM = "pageNum";
    public static final String PAGE_SIZE = "pageSize";

    /**
     * 提示信息
     */
    public static final String CATEGORY_NAME_EXISTS = "该分类名称已存在";

    /**
     * 存入redis的前缀 用来标识全局part进度
     */
    public static final String VIDEO_SLICE_REDIS_PREFIX = "vi:video:slice:";

    /**
     * 分片前缀 用来存储已上传的part
     */
    public static final String VIDEO_SLICE_PREFIX = "slice:";

    /**
     * 记录进度的存储时间
     */
    public static final Long VIDEO_SLICE_REDIS_EXPIRE = 24L;

    /**
     * 存储分片标识的存储时间
     */
    public static final Integer VIDEO_SLICE_REDIS_EXPIRE_SECONDS = 24;
}
