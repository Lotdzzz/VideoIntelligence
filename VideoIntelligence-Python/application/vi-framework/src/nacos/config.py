from application_config import *
from v2.nacos import (
    ClientConfigBuilder,
    GRPCConfig,
    ConfigParam,
)

# nacos客户端
naCos_client = (
    ClientConfigBuilder()
    .server_address(naCos.naCos_server_host_port)
    .namespace_id(naCos.naCos_namespace)
    .log_level("INFO")
    .grpc_config(GRPCConfig(grpc_timeout=5000))
    .build()
)

# 声明读取nacos配置中心的文件
naCos_config_center = ConfigParam(
    data_id="application-file.yaml", group="VI_GROUP"
)
