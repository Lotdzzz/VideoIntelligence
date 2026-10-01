from dataclasses import dataclass
from os import getenv


# naCos配置
@dataclass(frozen=True)
class NaCosConfig:
    naCos_server_host_port = getenv("NACOS_SERVER_HOST_PORT")
    naCos_server_ip = getenv("NACOS_SERVER_IP")
    naCos_default_group = getenv("NACOS_DEFAULT_GROUP", "DEFAULT_GROUP")
    naCos_namespace = getenv("NACOS_NAMESPACE", "public")
    naCos_service_name = "python-service"
    service_file_python = getenv("SERVICE_FILE_PYTHON")


naCos = NaCosConfig()


# rabbitmq配置
@dataclass(frozen=True)
class RabbitMQConfig:
    rabbitmq_server_host = getenv("RABBITMQ_SERVER_HOST")
    rabbitmq_server_port = getenv("RABBITMQ_SERVER_PORT")
    rabbitmq_server_username = getenv("RABBITMQ_SERVER_USERNAME")
    rabbitmq_server_password = getenv("RABBITMQ_SERVER_PASSWORD")
    rabbitmq_vhost = getenv("RABBITMQ_VHOST", "/")
    queue_routing_key = "file.analysis"


# minio配置
@dataclass(frozen=True)
class MinioConfig:
    minio_access_key = getenv("MINIO_ACCESS_KEY")
    minio_secret_key = getenv("MINIO_SECRET_KEY")
