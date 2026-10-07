from cosumer.analysis_consumer import consumer
from cosumer.thread_factory.consumer_thread_pool import worker_destroy, worker_pool_init
from cosumer.thread_factory.task_thread_pool import task_pool_init, task_pool_destroy


# 初始化启动线程池
async def init_thread_pool():
    # 启动mq的消费者线程组
    await worker_pool_init(consumer)
    # 启动任务管理器线程池
    await task_pool_init()


# 关机线程池
async def destroy_thread_pool():
    # 关闭mq的消费者线程组
    await worker_destroy()
    # 管理任务管理器线程池
    await task_pool_destroy()
