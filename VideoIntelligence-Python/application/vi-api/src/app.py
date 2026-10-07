from cosumer.thread_factory.thread_pool_manager import init_thread_pool, destroy_thread_pool
from minio_config.service import minio_starter
from nacos.service import *
from contextlib import asynccontextmanager
from fastapi import FastAPI
from rabbitmq.service import rabbitmq_starter, rabbitmq_unregister


# 运行时的活动
async def before_yield(_app: FastAPI):
    # 启动naCos以及挂载配置中心
    await nacos_starter(app=_app)
    # 注册minio
    await minio_starter()
    # 启动rabbitmq 创建消费者
    await rabbitmq_starter(app=_app)
    # 启动线程池
    await init_thread_pool()


# 结束后的活动
async def after_yield(_app: FastAPI):
    # 销毁naCos
    await nacos_deregister(app=_app)
    # 销毁mq
    await rabbitmq_unregister(app=_app)
    # 销毁线程池
    await destroy_thread_pool()


# 创建全局异步上下文管理器
@asynccontextmanager
async def lifespan(_app: FastAPI):
    await before_yield(_app)
    try:
        yield
    finally:
        await after_yield(_app)


"""
把定义好的生命周期逻辑绑定到这个 FastAPI 应用上。
FastAPI 应用启动时，会自动执行 yield 之前的代码（比如连接数据库、加载模型）。
FastAPI 应用关闭时，会自动执行 yield 之后的代码（比如断开数据库、释放资源）。
"""
app = FastAPI(lifespan=lifespan)
