from dataclasses import dataclass
from os import getenv

# nacos配置
@dataclass(frozen=True)
class NacosConfig:
    nacos_server_host_port = getenv("NACOS_SERVER_HOST_PORT")
    nacos_server_ip = getenv("NACOS_SERVER_IP")
    nacos_default_group = getenv("NACOS_DEFAULT_GROUP", "DEFAULT_GROUP")
    nacos_namespace = getenv("NACOS_NAMESPACE", "public")
    nacos_service_name = "python-service"
    service_file_python = getenv("SERVICE_FILE_PYTHON", 1002)