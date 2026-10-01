import json
import threading
import pika
from application_config import RabbitMQConfig
from entity.schemas.dto.file_dto import ViFileDTO
from exception.system_exception import SystemException
from rabbitmq.config import parameters
from service.pre_handler.pre_handler import video_pre_handler


# 定义回调方法
def call_back(ch, method, properties, body):
    """处理接收到的消息"""
    try:
        data = json.loads(body)
        vi_file = ViFileDTO.model_validate(data)
        video_pre_handler(video=vi_file)

        # 手动 ACK，确认消息已被处理
        ch.basic_ack(delivery_tag=method.delivery_tag)
    except Exception as e:
        print(e)
        # 处理失败，拒绝消息并重新入队（或者根据业务逻辑决定是否丢弃）
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
        raise SystemException(msg="RabbitMQ Call back error")


# 开始消费消息
def consumer():
    # 重新建立连接 不干扰主线程连接
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()
    channel.basic_qos(prefetch_count=1)
    channel.basic_consume(
        queue=RabbitMQConfig.queue_routing_key,
        on_message_callback=call_back,
    )
    try:
        channel.start_consuming()
    except Exception as e:
        print(f"消费者运行异常: {e}")
        raise SystemException(msg="RabbitMQ Running error")
    finally:
        connection.close()


# 另起线程启动消费者
async def analysis_consumer_starter():
    # 在这里新建连接与通道解开与注册线程的耦合 并守护主线程
    threading.Thread(
        target=consumer,
        daemon=True,
    ).start()
