import yaml
from contextlib import asynccontextmanager
from fastapi import FastAPI
from v2.nacos import NacosNamingService, NacosConfigService
from config.nacos_config import nacos_client, nacos_register, nacos_deregister, nacos_config_center
from config.rabbitmq_config import mq_client
from service.consumer.file_consumer import run_consumer_in_thread


# 创建namingService实例并生成注册和销毁方法 nacos加入fastapi的生命周期初始化
@asynccontextmanager
async def lifespan(_app: FastAPI):
    # 注册nacos
    naming_service = await NacosNamingService.create_naming_service(nacos_client)
    await naming_service.register_instance(nacos_register)

    # 拉取 Nacos 配置中心 YAML
    config_service = await NacosConfigService.create_config_service(nacos_client)
    config_str = await config_service.get_config(nacos_config_center)
    file_dict = yaml.safe_load(config_str)

    # 把配置挂到 app.state 上，其它模块通过 request.app.state.xxx 访问
    _app.state.file_config = file_dict

    # 启动rabbitmq
    mq_client.connect()
    run_consumer_in_thread()
    try:
        yield
    finally:
        # 注销nacos
        await naming_service.deregister_instance(nacos_deregister)
        await config_service.shutdown()
        # 断开rabbitmq
        mq_client.close()


# 挂载nacos以及api接口
app = FastAPI(lifespan=lifespan, debug=False)

# 创建好app后再导入模块 避免异步冲突
from api import file
# 注册全局异常处理器
from config.exception import register_exception_handlers  # noqa: E402

register_exception_handlers(app)

app.include_router(file.router)
