from minio import Minio

from common import MinioConfig


# 读取minio的配置 配置客户端
def register_minio(minio_config: dict) -> Minio:
    return Minio(
        endpoint=_normalize_endpoint(minio_config["endpoint"]),
        access_key=MinioConfig.minio_access_key,
        secret_key=MinioConfig.minio_secret_key,
        secure=False,
    )


# 去掉协议前缀和尾部斜杠、路径
def _normalize_endpoint(endpoint: str) -> str:
    if "://" in endpoint:
        endpoint = endpoint.split("://", 1)[1]
    endpoint = endpoint.split("/", 1)[0]
    return endpoint.rstrip("/")
