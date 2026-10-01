from minio import Minio
from application_config import MinioConfig
from resources.global_resources import resources


# 读取minio的配置 配置客户端
def create_minio_client() -> Minio:
    minio_client = Minio(
        endpoint=_normalize_endpoint(resources.minio.endpoint),
        access_key=MinioConfig.minio_access_key,
        secret_key=MinioConfig.minio_secret_key,
        secure=False,
    )
    return minio_client


# 去掉协议前缀和尾部斜杠、路径
def _normalize_endpoint(endpoint: str) -> str:
    if "://" in endpoint:
        endpoint = endpoint.split("://", 1)[1]
    endpoint = endpoint.split("/", 1)[0]
    return endpoint.rstrip("/")
