import pika
from application_config import RabbitMQConfig

# 注册mq的参数
parameters = pika.ConnectionParameters(
    host=RabbitMQConfig.rabbitmq_server_host,
    port=RabbitMQConfig.rabbitmq_server_port,
    virtual_host=RabbitMQConfig.rabbitmq_vhost,
    credentials=pika.PlainCredentials(
        RabbitMQConfig.rabbitmq_server_username,
        RabbitMQConfig.rabbitmq_server_password
    ),
    heartbeat=600,
    blocked_connection_timeout=300,
)