import json
import pika
from typing import Any

from application_config import RabbitMQConfig
from rabbitmq.config import parameters


def publish_analysis_result(data: dict[str, Any]):
    connection = None

    try:
        # 1. 创建新的 RabbitMQ 连接
        connection = pika.BlockingConnection(parameters)

        # 2. 创建 Channel
        channel = connection.channel()

        # 3. 发送 JSON 消息
        channel.basic_publish(
            exchange=RabbitMQConfig.analysis_result_exchange,
            routing_key=RabbitMQConfig.analysis_result_queue,
            body=json.dumps(
                data,
                ensure_ascii=False
            ).encode("utf-8"),
            properties=pika.BasicProperties(
                delivery_mode=2,
                content_type="application/json"
            )
        )

        print("RabbitMQ 消息发送成功")

    except Exception as e:
        raise RuntimeError(f"RabbitMQ 消息发送失败: {e}") from e

    finally:
        # 4. 关闭连接
        if connection and connection.is_open:
            connection.close()
