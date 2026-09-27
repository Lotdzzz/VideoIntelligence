# config/resources.py
from typing import Optional
from minio import Minio


# 全局资源管理器
class Resources:
    minio: Optional[Minio]


resources = Resources()
