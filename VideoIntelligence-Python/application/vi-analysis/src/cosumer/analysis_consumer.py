import json
import pika
import task.dispatcher.task_dispatcher as task_dispatcher
from application_config import RabbitMQConfig
from service.video_handler.video_handler import video_handler
from task.thread_factory.consumer_thread_pool import worker_consumers
from entity.model.consumer_worker import WorkerConsumer
from entity.schemas.dto.file_dto import ViFileDTO
from exception.system_exception import SystemException
from rabbitmq.config import parameters

"""
本模块的主要任务是初始化指定数量的worker线程之后
worker接收到任务就丢给task dispatcher调度器
调度器内部维护轮询机制去分配任务给aio线程组
"""


# 定义回调方法
def call_back(ch, method, properties, body):
    """处理接收到的消息"""
    try:
        data = json.loads(body)
        vi_file = ViFileDTO.model_validate(data)

        if task_dispatcher.global_task_dispatcher is None:
            print(f"task_dispatcher is none")
            return

        # 使用调度器分配任务获取结果
        future = task_dispatcher.global_task_dispatcher.submit(video_handler(vi_file))

        # 异步回调ack 当任务完成后才会ack
        def task_done(f):
            try:
                # 获取任务结果
                f.result()
                # 注意：这里不是直接 ack
                ch.connection.add_callback_threadsafe(
                    lambda: ch.basic_ack(
                        delivery_tag=method.delivery_tag
                    )
                )
            except Exception as e:
                print(f"task failed: {e}")
                ch.connection.add_callback_threadsafe(
                    lambda: ch.basic_nack(
                        delivery_tag=method.delivery_tag,
                        requeue=True
                    )
                )

        future.add_done_callback(task_done)

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
