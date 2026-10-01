from dataclasses import dataclass


# minio属性配置
@dataclass(frozen=True)
class MinioProperties:
    # MinIO服务的端点URL
    endpoint: str
    # 视频文件桶名称
    video_upload_bucket_name: str
    # 封面文件桶名称
    cover_upload_bucket_name: str
