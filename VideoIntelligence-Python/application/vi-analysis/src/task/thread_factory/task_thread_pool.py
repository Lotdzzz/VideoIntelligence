from constants.rabbitmq_constants import TASK_COUNTS
from entity.model.async_io_model import AsyncWorker
from task.dispatcher.task_dispatcher import task_dispatcher_init

# 全局一次
async_workers: list[AsyncWorker] = []

"""
将任务调度器设置为aio线程的目的是让任务可以不阻塞的执行
比如某个任务执行到等待网络io的时期 这时候用await等待回复时
aio就可以去执行别的任务了 不用死等网络io的返回
"""


# 任务管理器线程池初始化
# 使用异步io来处理任务
# 即任务从worker内进入到这里的loop循环
# loop循环可以同时处理大量的任务 worker可以把接收到的任务塞进loop内
async def task_pool_init():
    global async_workers
    async_workers = [AsyncWorker(i) for i in range(TASK_COUNTS)]
    for worker in async_workers:
        worker.start()
        print(f"task {worker.worker_id} aio thread loop initialized")
    # 初始化任务调度器
    task_dispatcher_init(aio_workers=async_workers)

# 任务管理器线程池销毁
async def task_pool_destroy():
    global async_workers
    for worker in async_workers:
        if worker.loop is not None:
            worker.loop.call_soon_threadsafe(worker.loop.stop)

        # 等待子线程真正退出（loop 停止后 run_forever 会返回）
        worker.thread.join(timeout=5)

        # loop 已停止 close
        if not worker.loop.is_closed():
            worker.loop.close()
            print(f"task {worker.worker_id} aio loop destroyed")
