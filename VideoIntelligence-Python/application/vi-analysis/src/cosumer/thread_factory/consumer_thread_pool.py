import threading
from constants.rabbitmq_constants import WORKER_COUNTS
from entity.model.consumer_worker import WorkerConsumer

# 工作组连接 用于优雅关机
worker_consumers: list[WorkerConsumer | None] = [None] * WORKER_COUNTS


# 在这里新建连接与通道解开与注册线程的耦合
# 创建线程消费者工作组 异步处理任务
async def worker_pool_init(task):
    for worker_id in range(WORKER_COUNTS):
        threading.Thread(
            target=task,
            args=(worker_id,),
            daemon=True,
        ).start()
    print(f"started {WORKER_COUNTS} workers")


# 关机
async def worker_destroy():
    # 关闭通道
    for worker in worker_consumers:
        if worker is None:
            continue
        try:
            worker.connection.add_callback_threadsafe(
                worker.channel.stop_consuming
            )
            print(f"stopped {WORKER_COUNTS} workers")
        except Exception as e:
            print(f"[Worker-{worker.worker_id}] stop failed: {e}")
