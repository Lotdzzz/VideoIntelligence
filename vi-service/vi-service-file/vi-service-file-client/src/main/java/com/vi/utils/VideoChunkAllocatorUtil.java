package com.vi.utils;

/**
 * 视频上传分片分配工具类
 * 支持 0 ~ 500GB，大小以字节为单位
 * 只返回分片大小和分片数量
 *
 * @author dotm
 */
public class VideoChunkAllocatorUtil {
    // ================== 单位封装 ==================
    public static final long B = 1L;
    public static final long KB = 1024L;
    public static final long MB = 1024L * KB;
    public static final long GB = 1024L * MB;
    public static final long TB = 1024L * GB;

    public enum SizeUnit {
        B(VideoChunkAllocatorUtil.B),
        KB(VideoChunkAllocatorUtil.KB),
        MB(VideoChunkAllocatorUtil.MB),
        GB(VideoChunkAllocatorUtil.GB),
        TB(VideoChunkAllocatorUtil.TB);

        private final long bytes;

        SizeUnit(long bytes) {
            this.bytes = bytes;
        }

        public long toBytes(long value) {
            return value * bytes;
        }
    }

    // ================== 分片策略常量 ==================
    /**
     * 最大支持 500GB
     */
    public static final long MAX_FILE_SIZE = 500 * GB;
    /**
     * 对象存储常见最大分片数
     */
    public static final int MAX_CHUNK_COUNT = 10000;
    /**
     * 最小分片大小，最后一片除外
     */
    public static final long MIN_CHUNK_SIZE = 5 * MB;

    /**
     * 私有构造函数，防止实例化
     */
    private VideoChunkAllocatorUtil() {
    }

    // ================== 核心方法 ==================

    /**
     * 根据文件大小自动分配分片
     *
     * @param fileSize 文件大小，单位：字节
     * @return 分片计划
     */
    public static ChunkPlan allocate(long fileSize) {
        return allocate(fileSize, MAX_CHUNK_COUNT);
    }

    /**
     * 使用指定单位分配分片
     *
     * @param value 文件大小数值
     * @param unit  单位
     */
    public static ChunkPlan allocate(long value, SizeUnit unit) {
        return allocate(unit.toBytes(value));
    }

    /**
     * 根据文件大小、最大分片数分配分片
     *
     * @param fileSize      文件大小，单位：字节
     * @param maxChunkCount 最大分片数
     */
    public static ChunkPlan allocate(long fileSize, int maxChunkCount) {
        if (fileSize < 0) {
            throw new IllegalArgumentException("文件大小不能为负数");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("文件大小超过限制，最大支持 500GB");
        }
        if (fileSize == 0) {
            return new ChunkPlan(0, 0, 0);
        }

        // 1. 选择基础分片大小
        long targetChunkSize = chooseTargetChunkSize(fileSize);
        long chunkCount = ceilDiv(fileSize, targetChunkSize);

        // 2. 如果分片数超过最大限制，动态增大分片大小
        if (chunkCount > maxChunkCount) {
            targetChunkSize = ceilDiv(fileSize, maxChunkCount);
            targetChunkSize = ceilToMB(targetChunkSize);
            if (targetChunkSize < MIN_CHUNK_SIZE) {
                targetChunkSize = MIN_CHUNK_SIZE;
            }
            chunkCount = ceilDiv(fileSize, targetChunkSize);
        }

        // 3. 小文件不分片
        if (chunkCount <= 1) {
            return new ChunkPlan(fileSize, fileSize, 1);
        }

        return new ChunkPlan(fileSize, targetChunkSize, chunkCount);
    }

    /**
     * 根据文件大小选择基础分片大小
     */
    private static long chooseTargetChunkSize(long fileSize) {
        if (fileSize <= 10 * MB) {
            return fileSize;               // 小于等于 10MB，不分片
        } else if (fileSize <= 100 * MB) {
            return 5 * MB;
        } else if (fileSize <= 1 * GB) {
            return 10 * MB;
        } else if (fileSize <= 10 * GB) {
            return 20 * MB;
        } else if (fileSize <= 50 * GB) {
            return 50 * MB;
        } else if (fileSize <= 100 * GB) {
            return 100 * MB;
        } else {
            return 200 * MB;               // 100GB ~ 500GB
        }
    }

    private static long ceilDiv(long a, long b) {
        return (a + b - 1) / b;
    }

    private static long ceilToMB(long bytes) {
        return ceilDiv(bytes, MB) * MB;
    }

    /**
     * 字节数转可读字符串
     */
    public static String readableSize(long bytes) {
        if (bytes < KB) {
            return bytes + " B";
        } else if (bytes < MB) {
            return String.format("%.2f KB", bytes / (double) KB);
        } else if (bytes < GB) {
            return String.format("%.2f MB", bytes / (double) MB);
        } else if (bytes < TB) {
            return String.format("%.2f GB", bytes / (double) GB);
        } else {
            return String.format("%.2f TB", bytes / (double) TB);
        }
    }

    // ================== 返回结果 ==================
    public static class ChunkPlan {
        private final long fileSize;
        private final long chunkSize;
        private final long chunkCount;

        public ChunkPlan(long fileSize, long chunkSize, long chunkCount) {
            this.fileSize = fileSize;
            this.chunkSize = chunkSize;
            this.chunkCount = chunkCount;
        }

        public long getFileSize() {
            return fileSize;
        }

        public long getChunkSize() {
            return chunkSize;
        }

        public long getChunkCount() {
            return chunkCount;
        }

        public String getFileSizeReadable() {
            return readableSize(fileSize);
        }

        public String getChunkSizeReadable() {
            return readableSize(chunkSize);
        }

        @Override
        public String toString() {
            return "ChunkPlan{" +
                    "fileSize=" + fileSize + " (" + getFileSizeReadable() + ")" +
                    ", chunkSize=" + chunkSize + " (" + getChunkSizeReadable() + ")" +
                    ", chunkCount=" + chunkCount +
                    '}';
        }
    }

    // ================== 快捷单位转换 ==================
    public static long mb(long value) {
        return value * MB;
    }

    public static long gb(long value) {
        return value * GB;
    }

    public static long tb(long value) {
        return value * TB;
    }
}
