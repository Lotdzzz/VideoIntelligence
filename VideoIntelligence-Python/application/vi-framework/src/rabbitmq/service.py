import pika
from typing import cast
from fastapi import FastAPI
from pika.connection import Connection
from application_config import RabbitMQConfig
from constants.rabbitmq_constants import CONNECTION, CHANNEL
from exception.system_exception import SystemException
from rabbitmq.config import parameters


# 初始化mq连接
async def rabbitmq_starter(app: FastAPI):
    # 从 app.state 中取出之前保存的实例
    connection = getattr(app.state, CONNECTION, None)
    channel = getattr(app.state, CHANNEL, None)

    # 判断之前是否已经连接
    if connection or channel:
        return

    try:
        connection = pika.BlockingConnection(parameters)
        channel = connection.channel()
        # 声明队列，durable=True 保证 RabbitMQ 重启后队列不丢失
        channel.queue_declare(queue=RabbitMQConfig.queue_routing_key, durable=True)

        # 将连接和通道保存到app
        app.state.connection = connection
        app.state.channel = channel
        print("RabbitMQ connection successfully")
    except Exception as e:
        raise SystemException(msg="RabbitMQ connection failed")


# 注销rabbitmq
async def rabbitmq_unregister(app: FastAPI):
    # 从 app.state 中取出之前保存的实例
    connection = cast(Connection | None, getattr(app.state, CONNECTION, None))

    if connection:
        connection.close()
        print("RabbitMQ close successfully")
