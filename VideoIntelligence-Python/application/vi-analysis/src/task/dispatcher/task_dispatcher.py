from entity.model.async_io_model import AsyncWorker
from entity.model.task_dispatcher_model import TaskDispatcher

# 全局单例调度者
global_task_dispatcher: TaskDispatcher | None = None


# 任务管理调度器
def task_dispatcher_init(aio_workers: list[AsyncWorker]):
    global global_task_dispatcher
    global_task_dispatcher = TaskDispatcher(aio_workers)
    print(f"Task dispatcher initialized workers = {aio_workers.__len__()}")