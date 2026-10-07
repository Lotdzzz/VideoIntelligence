import json
import pika
from application_config import RabbitMQConfig
from cosumer.thread_factory.consumer_thread_pool import worker_consumers
from entity.model.consumer_worker import WorkerConsumer
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
def consumer(worker_id: int):
    # 每个消费者一个连接 一个线程 保证任务处理不冲突
    # 重新建立连接 不干扰主线程连接
    connection = pika.BlockingConnection(parameters)
    channel = connection.channel()

    # 设置消费者处理信息
    channel.basic_qos(prefetch_count=10)
    channel.basic_consume(
        queue=RabbitMQConfig.queue_routing_key,
        on_message_callback=call_back,
    )

    # 存入消费者数组用于后续销毁
    worker_consumers[worker_id] = WorkerConsumer(
        channel=channel,
        connection=connection,
        worker_id=worker_id)
    print(f"[Worker-{worker_id}] consumer Init")

    try:
        # 阻塞开启
        channel.start_consuming()
    except Exception as e:
        print(f"消费者运行异常: {e}")
        raise SystemException(msg="RabbitMQ Running error")
    finally:
        try:
            if connection.is_open:
                connection.close()
                print(f"[Worker-{worker_id}] connection closed")
        except Exception as e:
            print(f"[Worker-{worker_id}] close error: {e}")
