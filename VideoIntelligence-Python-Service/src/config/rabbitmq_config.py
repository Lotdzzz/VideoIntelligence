import pika
from common import RabbitMQConfig


class RabbitMQClient:
    def __init__(self):
        self.credentials = pika.PlainCredentials(
            RabbitMQConfig.rabbitmq_server_username,
            RabbitMQConfig.rabbitmq_server_password)
        self.parameters = pika.ConnectionParameters(
            host=RabbitMQConfig.rabbitmq_server_host,
            port=RabbitMQConfig.rabbitmq_server_port,
            virtual_host=RabbitMQConfig.rabbitmq_vhost,
            credentials=self.credentials,
            heartbeat=600,
            blocked_connection_timeout=300,
        )
        self.connection = None
        self.channel = None

    def connect(self):
        """建立连接并声明队列"""
        try:
            if self.connection and not self.connection.is_closed:
                return
            self.connection = pika.BlockingConnection(self.parameters)
            self.channel = self.connection.channel()
            # 声明队列，durable=True 保证 RabbitMQ 重启后队列不丢失
            self.channel.queue_declare(queue=RabbitMQConfig.queue_routing_key, durable=True)
        except Exception as e:
            print(f"RabbitMQ 连接失败: {e}")
            raise

    def close(self):
        try:
            if self.connection and not self.connection.is_closed:
                self.connection.close()
        finally:
            pass


# 单例实例，供全局使用
mq_client = RabbitMQClient()
