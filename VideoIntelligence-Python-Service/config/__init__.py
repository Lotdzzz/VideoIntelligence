from contextlib import asynccontextmanager
from common import NacosConfig as nacos
from fastapi import FastAPI
from api import file
from v2.nacos import (
    ClientConfigBuilder,
    GRPCConfig,
    NacosNamingService,
    RegisterInstanceParam,
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


# 创建namingService实例并生成注册和销毁方法 加入fastapi的生命周期初始化
@asynccontextmanager
async def lifespan(_app: FastAPI):
    # 获取实例
    naming_service = await NacosNamingService.create_naming_service(nacos_client)
    await  naming_service.register_instance(
        RegisterInstanceParam(
            service_name=nacos.nacos_service_name,
            ip=nacos.nacos_server_ip,
            port=nacos.service_file_python,
            group_name=nacos.nacos_default_group,
            ephemeral=True,
        )
    )
    print("nacos register instance init")

    yield  # 应用运行期间

    # 注销实例
    await naming_service.unregister(
        RegisterInstanceParam(
            service_name=nacos.nacos_service_name,
            ip=nacos.nacos_server_ip,
            port=nacos.service_file_python,
            group_name=nacos.nacos_default_group,
            ephemeral=True,
        )
    )
    print("nacos unregister instance destroy")


# 挂载
app = FastAPI(lifespan=lifespan)
app.include_router(file.router)