from pydantic import BaseModel, Field
from datetime import datetime
from typing import Optional, Literal


# 接收到的视频文件DTO
class ViFileDTO(BaseModel):
    id: Optional[int] = None
    """文件ID（修改时必填）"""

    userId: Optional[int] = None
    """所属用户ID"""

    originalName: Optional[str] = None
    """原始文件名"""

    objectName: Optional[str] = None
    """MinIO对象名称"""

    bucketName: Optional[str] = None
    """MinIO Bucket 名称"""

    fileType: Optional[Literal["video", "audio", "image", "document", "mp4"]] = None
    """文件类型，如 video/audio/image/document"""

    fileExt: Optional[str] = None
    """文件扩展名，如 mp4/pdf/jpg"""

    contentType: Optional[str] = None
    """MIME类型，如 video/mp4"""

    cover: Optional[str] = None
    """封面图URL，适用于视频/音频/文档等"""

    fileSize: Optional[int] = None
    """文件大小，单位：字节"""

    categoryId: Optional[int] = None
    """文件分类ID"""

    status: Optional[int] = Field(default=None, ge=0, le=5)
    """文件状态：0上传中 1已上传 2处理中 3处理完成 4上传失败 5已删除"""

    md5: Optional[str] = None
    """文件MD5，用于后续秒传/去重"""

    uploadId: Optional[str] = None
    """uploadId，用于分片上传的唯一标识"""

    chunkCount: Optional[int] = None
    """分片总数"""

    chunkSize: Optional[int] = None
    """分片大小，单位：字节"""

    uploadTime: Optional[datetime] = None
    """上传完成时间"""

    beginTime: Optional[datetime] = None
    """创建时间-开始（用于查询过滤）"""

    endTime: Optional[datetime] = None
    """创建时间-结束（用于查询过滤）"""
