from concurrent.futures.thread import ThreadPoolExecutor
from constants.rabbitmq_constants import TASK_COUNTS

# 全局一次
task_executor: ThreadPoolExecutor | None = None


# 任务管理器线程池初始化
async def task_pool_init():
    global task_executor
    task_executor = ThreadPoolExecutor(max_workers=TASK_COUNTS)
    print("task pool initialized")


# 任务管理器线程池销毁
async def task_pool_destroy():
    if task_executor is not None:
        task_executor.shutdown(wait=True)
        print("task pool destroyed")