from typing import Optional

from resources.properties.minio_properties import MinioProperties


# 全局资源管理器 作用就是把一些配置中心的数据拿到这里供全局文件使用
class Resources:
    # 配置中心
    config: Optional[dict] = None
    # minio配置
    minio: Optional[MinioProperties] = None


resources = Resources()
