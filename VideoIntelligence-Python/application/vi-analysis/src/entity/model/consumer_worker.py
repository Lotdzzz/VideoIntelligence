# 接收到的视频文件DTO
from pika.adapters.blocking_connection import BlockingChannel, BlockingConnection
from pydantic import BaseModel, ConfigDict


# 消费者
class WorkerConsumer(BaseModel):
    # 允许任何类型
    model_config = ConfigDict(arbitrary_types_allowed=True)

    # 通道
    channel: BlockingChannel
    # 连接
    connection: BlockingConnection
    # 工作id
    worker_id: int
