import json
import threading

from config.exception import SystemException
from config.rabbitmq_config import RabbitMQClient
from common import RabbitMQConfig


def callback(ch, method, properties, body):
    """处理接收到的消息"""
    try:
        data = json.loads(body)
        # TODO: 在这里写你的业务逻辑，比如调用 service 里的处理函数

        # 手动 ACK，确认消息已被处理
        ch.basic_ack(delivery_tag=method.delivery_tag)
    except Exception as e:
        print(e)
        # 处理失败，拒绝消息并重新入队（或者根据业务逻辑决定是否丢弃）
        ch.basic_nack(delivery_tag=method.delivery_tag, requeue=True)
        raise SystemException(msg="RabbitMQ Call back error")


def start_consumer():
    """启动消费者（阻塞）"""
    # 消费者使用全新的独立连接实例
    consumer_client = RabbitMQClient()
    consumer_client.connect()
    channel = consumer_client.channel

    # 每次只预取一条消息，处理完再取，实现公平分发
    channel.basic_qos(prefetch_count=1)

    channel.basic_consume(
        queue=RabbitMQConfig.queue_routing_key,
        on_message_callback=callback
    )
    try:
        channel.start_consuming()
    except Exception as e:
        print(f"消费者运行异常: {e}")
        raise SystemException(msg="RabbitMQ Running error")
    finally:
        consumer_client.close()


def run_consumer_in_thread():
    """在后台线程中运行消费者，避免阻塞主线程"""
    thread = threading.Thread(target=start_consumer, daemon=True)
    thread.start()
