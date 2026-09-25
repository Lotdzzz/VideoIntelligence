from fastapi import FastAPI
from api import file
from contextlib import asynccontextmanager
from v2.nacos import NacosNamingService
from config.nacos_config import nacos_client, nacos_register, nacos_deregister
from config.rabbitmq_config import mq_client
from service.consumer.file_consumer import run_consumer_in_thread


# 创建namingService实例并生成注册和销毁方法 nacos加入fastapi的生命周期初始化
@asynccontextmanager
async def lifespan(_app: FastAPI):
    # 注册nacos
    naming_service = await NacosNamingService.create_naming_service(nacos_client)
    await naming_service.register_instance(nacos_register)
    # 启动rabbitmq
    mq_client.connect()
    run_consumer_in_thread()
    try:
        yield
    finally:
        # 注销nacos
        await naming_service.deregister_instance(nacos_deregister)
        #断开rabbitmq
        mq_client.close()


# 挂载nacos以及api接口
app = FastAPI(lifespan=lifespan)
app.include_router(file.router)