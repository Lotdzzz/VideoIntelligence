from common import NacosConfig as nacos
from v2.nacos import (
    ClientConfigBuilder,
    GRPCConfig,
    RegisterInstanceParam,
    DeregisterInstanceParam,
    ConfigParam,
)

# nacos客户端
nacos_client = (
    ClientConfigBuilder()
    .server_address(nacos.nacos_server_host_port)
    .namespace_id(nacos.nacos_namespace)
    .log_level("INFO")
    .grpc_config(GRPCConfig(grpc_timeout=5000))
    .build()
)

nacos_register = RegisterInstanceParam(
    service_name=nacos.nacos_service_name,
    # ip=nacos.nacos_server_ip,
    ip="10.10.80.118",
    port=nacos.service_file_python,
    group_name=nacos.nacos_default_group,
    ephemeral=True,
)

nacos_deregister = DeregisterInstanceParam(
    service_name=nacos.nacos_service_name,
    ip=nacos.nacos_server_ip,
    port=nacos.service_file_python,
    group_name=nacos.nacos_default_group,
    ephemeral=True,
)

# 声明读取nacos配置中心的文件
nacos_config_center = ConfigParam(
    data_id="application-file.yaml", group="VI_GROUP"
)
