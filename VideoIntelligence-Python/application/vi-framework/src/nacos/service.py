import yaml
from fastapi import FastAPI
from v2.nacos import RegisterInstanceParam, DeregisterInstanceParam, NacosNamingService, NacosConfigService
from nacos.config import *
from resources.global_resources_manager import init_resources
from utils.ip_util import get_ip

# 注册方法
naCos_register = RegisterInstanceParam(
    service_name=naCos.naCos_service_name,
    # ip=nacos.nacos_server_ip,
    ip=get_ip(),
    port=naCos.service_file_python,
    group_name=naCos.naCos_default_group,
    ephemeral=True,
)

# 销毁方法
naCos_deregister = DeregisterInstanceParam(
    service_name=naCos.naCos_service_name,
    # ip=naCos.naCos_server_ip,
    ip=get_ip(),
    port=naCos.service_file_python,
    group_name=naCos.naCos_default_group,
    ephemeral=True,
)


# naCos启动方法
async def nacos_starter(app: FastAPI):
    # 注册nacos
    naming_service = await NacosNamingService.create_naming_service(naCos_client)
    await naming_service.register_instance(naCos_register)

    # 拉取 Nacos 配置中心 YAML
    config_service = await NacosConfigService.create_config_service(naCos_client)
    config = await config_service.get_config(naCos_config_center)
    # 挂载配置中心到全局资源管理器中
    await init_resources(yaml.safe_load(config))

    # 挂载需要销毁的实例
    app.state.naCos_naming_service = naming_service
    app.state.naCos_config_service = config_service
    print("naCos started successfully")


# naCos销毁方法
async def nacos_deregister(app: FastAPI):
    # 从 app.state 中取出之前保存的实例
    naming_service = getattr(app.state, "naming_service", None)
    config_service = getattr(app.state, "config_service", None)
    # 注销 Nacos
    if naming_service:
        await naming_service.deregister_instance(naCos_deregister)
    # 关闭配置中心客户端
    if config_service:
        await config_service.shutdown()

    print("nacos deregistered successfully")
